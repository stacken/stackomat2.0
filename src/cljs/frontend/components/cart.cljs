(ns frontend.components.cart
  (:require [frontend.events :as events]))

(defn cart-table-body [state]
  [:tbody
   (doall (for [product (sort (fn [e1 e2] (< (:key e1) (:key e2)))
                              (:chosen-products @state))]
            ^{:key (.indexOf (:chosen-products @state) product)}
            [:tr 
             [:td (:name product)]
             [:td (:price product)]
             [:td [:span {:class "glyphicon glyphicon-remove"
                          :onClick #(events/send-event (:channel @state) :cart :remove-product product)}]]]))])

(defn cart-table-footer [state]
  [:tfoot
   [:tr
    [:th {:class "col-sm-8"}]
    [:th {:class "col-sm-2"} 
     (if (not (empty? (:chosen-products @state)))
       [:span "Totalt " (reduce + (map :price (:chosen-products @state)))])]]])

(defn cart [state]
  (fn []
    [:div 
     (if (empty? (:chosen-products @state))
       [:p "Inga produkter har valts."]
       [:table {:class "table table-striped col-sm-12"}
        [:thead
         [:tr
          [:th {:class "col-sm-8"} "Vara"]
          [:th {:class "col-sm-2"} "Pris"]
          [:th {:class "col-sm-2"} "Ta bort"]]]
        [cart-table-body state]
        [cart-table-footer state]])]))
