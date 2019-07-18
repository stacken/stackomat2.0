(ns frontend.components.product-list
  (:require [reagent.core :as r]
            [clojure.string :as string]
            [frontend.http :as http]
            [frontend.events :as events]
            [frontend.state :as state]))

(defn focus-barcode-input! []
  (.focus (.getElementById js/document "barcodeInput")))


(defn filter-products [state search-value]
  (filter (fn [item]
            (or (= (:id item) search-value)
                (string/includes? (string/lower-case (:name item))
                                  (string/lower-case search-value))))
          (:all-products state)))

(defn submit-callback! [state search-value]
  (let [filtered-products (filter-products state search-value)]
    (if (= 1 (count filtered-products))
      (events/send-event (:channel state) 
                         :product-list 
                         :product-chosen (first filtered-products)))))

(defn clear-search [value]
  [:a [:span {:class "glyphicon glyphicon-remove btn btn-default"
              :onClick (fn [event] 
                         (.preventDefault event)
                         (focus-barcode-input!)
                         (reset! value ""))}]])

(defn show-all-products [should-show]
  [:a [:span {:class "glyphicon glyphicon-th-list btn btn-default"
              :style {:color (if @should-show "green")}
              :onClick (fn [event]
                         (.preventDefault event)
                         (focus-barcode-input!)
                         (reset! should-show (not @should-show)))
              }]])

(defn search-field [value submit-callback should-show-all-products]
  [:form {:class "form-inline"
          :on-submit (fn [event] 
                       (.preventDefault event)
                       (submit-callback @value)
                       (reset! value ""))}
   [:div {:class "form-group"}
    [show-all-products should-show-all-products]
    [:input {:type "text"
             :id "barcodeInput"
             :class "form-control"
             :value @value 
             :on-change (fn [event] (reset! value (-> event .-target .-value)))
             :placeholder "Scanna vara"}]
    [clear-search value]]])

(defn table-head []
  [:thead
   [:tr
    [:th {:class "col-sm-7"} "Produkt"]
    [:th {:class "col-sm-3"} "Pris"]
    [:th {:class "col-sm-2"} "Plocka"]]])

(defn table-body [state search-value]
  [:tbody
   (for [product (doall (filter-products @state @search-value))]
     ^{:key product}
     [:tr {:onClick (fn [event] 
                      (.preventDefault event)
                      (focus-barcode-input!)
                      (events/send-event (:channel @state) 
                                         :product-list 
                                         :product-chosen product))
           :class "product-list-row"}
      [:td (:name product)]
      [:td (:price product)]
      [:td [:span {:class "glyphicon glyphicon-plus"}]]])])

(defn product-list [state]
  (let [search-value (r/atom "")
        should-show-all-products (r/atom false)
        selectBarcodeInterval (r/atom nil)]
    (r/create-class
      {:display-name "product-list-with-atom"

       :component-did-mount 
       (fn [e] 
         (reset! selectBarcodeInterval (.setInterval js/window focus-barcode-input! 1000))
         (.focus (.getElementById js/document "barcodeInput")))

       :component-will-unmount
       (reset! selectBarcodeInterval (.clearInterval js/window selectBarcodeInterval))

       :reagent-render 
       (fn [state]
         [:div
          [search-field search-value #(submit-callback! @state %) should-show-all-products]
          [:div {:class "product-list"}
           (if @should-show-all-products
             [:table {:class "table table-striped col-sm-12"}
              [table-head]
              [table-body state search-value]])]])})))
