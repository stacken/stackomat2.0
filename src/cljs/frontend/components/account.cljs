(ns frontend.components.account
  (:require 
    [reagent.core :as reagent :refer [atom]]
    [frontend.events :as events]
    [frontend.state :as state]))

(defn charge-button [state amount]
  [:a {:class "btn btn-default" :on-click #(events/send-event (:channel @state)
                                                              :account
                                                              :money-chosen
                                                              amount)}
   "Ladda " amount])

(defn account [state]
  [:div
   [:span "Användarid: " (:user-id @state)]
   [:div
    (for [amount [1 2 5 10 20 50 100 200 500 1000]]
      ^{:key amount}
      [charge-button state amount])]
   [:div 
    [:a {:class "btn btn-default"}
     [:span {:class ""
             :on-click #(events/send-event (:channel @state)
                                           :account
                                           :add-money
                                           (:money-to-add @state))}
      " ¤ Ladda " (:money-to-add @state)]]
    [:a {:on-click #(events/send-event (:channel @state) :account-back-button :change-page {:page :purchase})
         :class "btn btn-default"}
     [:span {:class "glyphicon glyphicon-circle-arrow-left"}]
     " Tillbaka"]]])
