(ns frontend.components.account
  (:require 
    [reagent.core :as reagent :refer [atom]]
    [frontend.events :as events]
    [frontend.state :as state]))

(defn charge-button [state amount]
  [:div {:class "col-sm-3" :style {:margin "10px 0"}}
   [:a {:class "btn btn-default col-sm-12" 
        :on-click #(events/send-event (:channel @state)
                                      :account
                                      :money-chosen
                                      amount)}
    "Välj " amount " ☂"]])

(defn account [state]
  [:div
   [:span "Användarid: " (:user-id @state)]
   [:div {:class "row"}
    (for [amount [1 2 5 10 20 50 100 200 500 1000]]
      ^{:key amount}
      [charge-button state amount])]
   [:div {:class "row"
          :style {:padding "20px 0"}}
    [:div {:class "col-sm-2"}
     [:a {:on-click #(events/send-event (:channel @state) :account-back-button :change-page {:page :purchase})
          :class "btn btn-default"}
      [:span {:class "glyphicon glyphicon-circle-arrow-left"}]
      " Tillbaka"]]
    [:div {:class "col-sm-offset-7 col-sm-3"}
     [:a {:class "btn btn-default"}
      [:span {:class ""
              :on-click #(events/send-event (:channel @state)
                                            :account
                                            :add-money
                                            (:money-to-add @state))}
       " Ladda " (:money-to-add @state) " ☂"]]]]])
