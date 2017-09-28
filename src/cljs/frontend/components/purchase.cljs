(ns frontend.components.purchase
  (:require [frontend.http :as http]
            [frontend.components.product-list :refer [product-list]]
            [frontend.components.cart :refer [cart]]
            [frontend.components.purchase-control :refer [purchase-control]]
            [frontend.state :as state]))

(defn purchase [state]
  [:div 
   [:div {:class "row"}
    [:div {:class "col-sm-12"} [product-list state]]
    [:div {:class "col-sm-12"} [cart state]]]
   [:div {:class "row"}
    [purchase-control state]]])
