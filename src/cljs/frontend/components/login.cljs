(ns frontend.components.login
  (:require [frontend.events :as events]
            [reagent.core :as r]))

(defn login-input [state]
  (let [user-id (r/atom "")]
    (r/create-class 
      {:component-did-mount (fn [e]
                              (->> "login"
                                   (.getElementById js/document)
                                   .focus))
       :render (fn [] 
                 [:form {:class "login-form"
                         :on-submit (fn [event]
                                      (.preventDefault event)
                                      (events/send-event (:channel @state) :login :change-page {:page :purchase})
                                      (events/send-event (:channel @state) :product-list :user-id-entered @user-id))}
                  [:div {:class "form-group"}
                   [:input {:type "text" 
                            :id "login"
                            :class "form-control" 
                            :on-change #(reset! user-id (-> % .-target .-value))
                            :value @user-id 
                            :placeholder "Scanna ditt kort"}]]])})))

(defn login 
  ([state]
   (let [user-id (r/atom "")]
     (fn [state]
       [:div {:class "login-page"}
        [:div {:class "row"}
         [:h1 {:class "header"} "Stack O mat"]
         [:div {:class "row"}
          [:div {:class "col-sm-8 col-sm-offset-2"}
           [login-input state]]]]]))))
