(ns frontend.core
    (:require [reagent.core :as reagent :refer [atom]]
              [frontend.components.purchase :refer [purchase]]
              [frontend.components.account :refer [account]]
              [frontend.components.login :refer [login]]
              [frontend.components.message :refer [message]]
              [frontend.state :as state]
              [frontend.http :as http]
              [frontend.events :as events]
              [frontend.handlers :as handlers]
              [reagent.session :as session]
              [goog.events :as gevents]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; the app state
(defonce state (atom (state/make-state)))

;; -------------------------
;; Views

(defn login-page []
  [:div [login state]])

(defn purchase-page []
  [:div [purchase state]])

(defn account-page []
  [:div [account state]])

(defn message-page []
  [:div [message state]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'login-page))

(def handler-change-current-page
  {:name "change current page"
   :query '[:find ?page
            :where
            [?id :event/type :change-page]
            [?id :payload ?page]]
   :function (fn ([state payload]
                  (session/put! :current-page 
                                (cond (= :login (:page payload)) #'login-page
                                      (= :purchase (:page payload)) #'purchase-page
                                      (= :account (:page payload)) #'account-page
                                      (= :message (:page payload)) #'message-page
                                      true (session/get :current-page)))
                  state))})

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (events/listen (:channel @state) 
                 state
                 (conj handlers/handlers handler-change-current-page)
                 (* 30 1000))
  (http/http-get (:channel @state) "/products") 
  (mount-root))
