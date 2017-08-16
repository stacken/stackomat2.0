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
      (events/send-event (:channel state) :product-list :product-chosen (first filtered-products)))))

(defn search-field [value submit-callback]
  [:form {:on-submit (fn [event] 
                       (.preventDefault event)
                       (submit-callback @value)
                       (reset! value ""))}
   [:div {:class "form-group"}
    [:input {:type "text"
             :id "barcodeInput"
             :class "form-control"
             :value @value 
             :on-change (fn [event] (reset! value (-> event .-target .-value)))
             :placeholder "Scanna vara"}]]])

(defn table-head []
  [:thead
   [:tr
    [:th {:class "col-sm-7"} "Produkt"]
    [:th {:class "col-sm-3"} "Pris"]
    [:th {:class "col-sm-2"} "Plocka"]]])

(defn product-list [state]
  (let [search-value (r/atom "")]
    (r/create-class
      {:display-name "product-list-with-atom"
       :component-did-mount (fn [e] (.focus (.getElementById js/document "barcodeInput")))
       :reagent-render (fn [state]
                         [:div
                          [search-field search-value #(submit-callback! state %)]
                          [:table {:class "table table-striped col-sm-12"}
                           [table-head]
                           [:tbody
                            (for [product (doall (filter-products @state @search-value))]
                              ^{:key product}
                              [:tr {:onClick (fn [event] 
                                               (focus-barcode-input!)
                                               (events/send-event (:channel @state) :product-list :product-chosen product))}
                               [:td (:name product)]
                               [:td (:price product)]
                               [:td [:span {:class "glyphicon glyphicon-plus"}]]])]]])})))
