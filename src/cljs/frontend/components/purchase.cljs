(ns frontend.components.purchase
  (:require [frontend.http :as http]
            [frontend.components.product-list :refer [product-list]]
            [frontend.components.cart :refer [cart]]
            [frontend.components.message :refer [message]]
            [frontend.components.purchase-control :refer [purchase-control]]
            [frontend.state :as state]))

(defn purchase [state]
  [:div 
   [:div {:class "col-sm-4"}
    [product-list state]]
   [:div {:class "col-sm-8"} 
    [message state]
    [cart state]]
   [:div {:class "col-sm-12 purchase-control"} 
    [purchase-control state]]])
