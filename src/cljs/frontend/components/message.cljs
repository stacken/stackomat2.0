(ns frontend.components.message
  (:require [clojure.string :as string]
            [frontend.events :as events]))

(defn close-message-button [state]
  [:a {:class "btn btn-default" 
       :onClick #(events/send-event (:channel @state) 
                                    :message 
                                    :close-message)}
   "Stäng"])

(defn message [state]
  (let [msg (:message @state)
        text (:message msg)
        enabled (:enabled msg)
        title (:title msg)
        type (:type msg)]
    (if (and enabled (or (not (string/blank? text))
                         (not (string/blank? title))))
      [:div {:class "message-background row"}
       [:div {:class "message-content col-sm-8 col-sm-offset-2"}
          [:h2 {:class (cond (= :success type) "text-success"
                             (= :error type) "text-danger"
                             true "text-muted")}
           title]
          [:p text]
          [close-message-button state]]])))

; (let [text (get-in [:message :text] @state)
;       type (get-in [:message :type] @state)]
;   [:div 
;    (if (not (empty? text))
;      (cond (= :success type)
;            [:p {:class "text-success"} text]

;            (= :error type)
;            [:p {:class "text-danger"} text]

;            true
;            [:p {:class "text-muted"} text]))]))
