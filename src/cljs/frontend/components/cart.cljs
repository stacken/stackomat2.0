(ns frontend.components.cart
  (:require [frontend.events :as events]
            [frontend.components.message :refer [message]]))

(defn cart-table-head []
  [:thead
   [:tr
    [:th {:class "col-sm-8"} "Vara"]
    [:th {:class "col-sm-2"} "Pris"]
    [:th {:class "col-sm-2" :style {:text-align "right"}} "Ta bort"]]])

(defn cart-table-body [state]
  [:tbody (doall (for [product (sort (fn [e1 e2] (< (:key e1) (:key e2)))
                                     (:chosen-products @state))]
                   ^{:key (.indexOf (:chosen-products @state) product)}
                   [:tr 
                    [:td (:name product)]
                    [:td (.toFixed (:price product) 2) " ☂"]
                    [:td {:style {:text-align "right"}}
                     
                     [:span {:class "glyphicon glyphicon-remove"
                                 :onClick #(events/send-event (:channel @state) :cart :remove-product product)}]]]))])

(defn cart [state]
  [:div {:class "cart"}
   [message state]
   (if (empty? (:chosen-products @state))
     [:p "Inga produkter har valts."]
     [:table {:class "table table-striped"}
      [cart-table-head]
      [cart-table-body state]])])
