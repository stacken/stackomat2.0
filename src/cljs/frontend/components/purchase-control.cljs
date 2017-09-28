(ns frontend.components.purchase-control
  (:require [clojure.string :as string]
            [frontend.events :as events]
            [frontend.state :as state]))

(defn user-id-and-balance-viewer [state]
  [:p {:class "user-id"}
   " Användarid: " (:user-id @state) " "
   " Saldo " (if (:balance @state)
               (:balance @state)
               0)
   " ☂"])

(defn purchase-button [state]
  (let [total (reduce (fn [sum price]
                        (.toFixed (+ (js/parseFloat sum) price)
                                  2))
                      0
                      (map :price (:chosen-products @state)))]
    [:a {:class "btn btn-default purchase-button"
         :on-click #(events/send-event (:channel @state) :purchase-control :pay (state/sum-of-chosen-products @state))
         :disabled (empty? (:chosen-products @state))}
     [:span {:class "glyphicon glyphicon-ok-sign"}] " Betala " total " ☂"]))

(defn logout-button [state]
  [:a {:class "btn btn-default purchase-button" 
       :on-click (fn []
                   (events/send-event (:channel @state) :reset-button :change-page {:page :login})
                   (events/send-event (:channel @state) :purchase-control :reset-purchase))}
   [:span {:class "glyphicon glyphicon-remove-sign"}]
   " Logga ut"])

(defn manage-account-button [state]
  (if (not (string/blank? (:user-id @state)))
    [:a {:class "btn btn-default purchase-button" 
         :on-click #(events/send-event (:channel @state) :add-money-button :change-page {:page :account})}
     [:span {:class "glyphicon glyphicon-info-sign"}]
     " Ladda pengar"]))

(defn buy-or-message [state]
  (if (not (string/blank? (:user-id @state)))
    [purchase-button state]
    [:p "Vifta med ditt mifare-kort för att läsa användarid. Detta behövs för att kunna betala."]))

(defn purchase-control [state]
  [:div 
   [:div {:class "col-sm-12"}
    [user-id-and-balance-viewer state]]
   [:div {:class "col-sm-12"} 
    [buy-or-message state]
    [manage-account-button state]
    [logout-button state]]])
