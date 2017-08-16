(ns frontend.components.purchase-control
  (:require [clojure.string :as string]
            [frontend.events :as events]
            [frontend.state :as state]))

(defn user-id-and-balance-viewer [state]
  [:span
   " Användarid: " (:user-id @state) " "
   " Saldo " (if (:balance @state)
               (:balance @state)
               0)
   " "])

(defn purchase-button [state]
  [:a {:class "btn btn-default"
            :on-click #(events/send-event (:channel @state) :purchase-control :pay (state/sum-of-chosen-products @state))
            :disabled (empty? (:chosen-products @state))}
   [:span {:class "glyphicon glyphicon-ok-sign"}] " Betala"])

(defn reset-button [state]
  [:a {:class "btn btn-default" 
            :on-click (fn []
                        (events/send-event (:channel @state) :reset-button :change-page {:page :login})
                        (events/send-event (:channel @state) :purchase-control :reset-purchase))}
   [:span {:class "glyphicon glyphicon-remove-sign"}]
   " Logga ut"])

(defn manage-account-button [state]
  (if (not (string/blank? (:user-id @state)))
    [:a {:class "btn btn-default" 
         :on-click #(events/send-event (:channel @state) :add-money-button :change-page {:page :account})}
     [:span {:class "glyphicon glyphicon-info-sign"}]
     " Ladda pengar"]))

(defn purchase-control [state]
  [:div {:class "row"}
   [:div {:class "col-sm-8"}
    (if (not (string/blank? (:user-id @state)))
      [:p
       [purchase-button state]
       [user-id-and-balance-viewer state]]
      [:p "Vifta med ditt mifare-kort för att läsa användarid. Detta behövs för att kunna betala."])]
   [:div {:class "col-sm-2"}
    [manage-account-button state]]
   [:div {:class "col-sm-2"}
    [reset-button state]]])
