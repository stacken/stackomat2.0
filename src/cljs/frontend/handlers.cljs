(ns frontend.handlers
  (:require [frontend.state :as state]
            [frontend.http :as http]
            [frontend.events :as events]))

;; Each handler is composed of three things;
;;   1. a datalog query, given as :query in the handler functions metadata
;;   2. a handler function
;;
;; If the query when run against an event is not empth, the handler function is 
;; called with the current app state and the datalog query results as first and
;; second arguments, respectively.
;;
;; Each handler must not have any side effects other than doing http requests 
;; (via frontend.http) or sending events with events/send-event.
;;
;; If the handler returns a map it MUST return the state it was given (though
;; it may have been modified). Other return values will be disregarded.
;;
;; A handler may return a function in which case this function is called with
;; the same arguments as above.

(def handlers 
  [{:name "set-user-id "
    :query '[:find ?user-id
             :where
             [?id :event/type :user-id-entered]
             [?id :payload ?user-id]]
    :function (fn ([state user-id]
                   (state/set-user-id state user-id)))}

   {:name "fetch-balance-from-user-id"
    :query '[:find ?user-id
             :where
             [?id :event/type :user-id-entered]
             [?id :payload ?user-id]]
    :function (fn ([state user-id]
                   (http/http-get (:channel state) (str "/user/" user-id "/balance"))
                   state))}

   {:name "set-balance-from-http "
    :query '[:find ?body ?url
             :in $ ?url-is-valid
             :where
             [?id :body ?body]
             [?id :event/type :http-response]
             [?id :success true]
             [?id :url ?url]
             [(?url-is-valid ?url)]]

    :query-ins (fn [url]
                 (or (re-matches #"/user/\w+/balance" url)
                     (re-matches #"/user/\w+/pay" url)
                     (re-matches #"/user/\w+/addmoney" url)))
    :function (fn ([state body url]
                   (events/send-event (:channel state)
                                      :http-get-balance
                                      :have-balance
                                      (if (nil? (:balance body))
                                        0
                                        (:balance body)))
                   state))}

   {:name "set-products "
    :query '[:find ?products
             :where
             [?id :event/type :http-response]
             [?id :url "/products"]
             [?id :success true]
             [?id :body ?products]]
    :function (fn ([state products]
                   (state/set-products state products)))}

   {:name "choose-product "
    :query '[:find ?chosen-product
             :where
             [?id :event/type :product-chosen]
             [?id :payload ?chosen-product]]
    :function (fn ([state chosen-product]
                   (state/choose-product state chosen-product)))}

   {:name "remove-product"
    :query '[:find ?removed-product
             :where
             [?id :event/type :remove-product]
             [?id :payload ?removed-product]]
    :function (fn ([state product-to-remove]
                   (state/remove-product state product-to-remove)))}

   {:name "pay"
    :query '[:find ?amount
             :where
             [?id :event/type :pay]
             [?id :payload ?amount]]
    :function (fn ([state amount]
                   (http/http-post (:channel state)
                                   (str "/user/" (:user-id state) "/pay")
                                   {:amount amount})
                   state))}

   {:name "paid-successfully"
    :query '[:find ?response
             :where
             [?id :event/type :http-response]
             [?id :url ?url]
             [(re-matches #"/user/\w+/pay" ?url)]
             [?id :status 200]
             [?id :body ?response]]
    :function (fn ([state response]
                   (events/send-event (:channel state)
                                      :pay-not-enough-money
                                      :set-message 
                                      {:type :success
                                       :message (str "Du betalade " (:amount response))})
                   state))}

   {:name "pay-not-enough-money"
    :query '[:find ?response
             :where
             [?id :event/type :http-response]
             [?id :url ?url]
             [(re-matches #"/user/\w+/pay" ?url)]
             [?id :status 403]
             [?id :body ?response]]
    :function (fn ([state response]
                   (events/send-event (:channel state)
                                      :pay-not-enough-money
                                      :set-message 
                                      {:type :error
                                       :message (:message response)})
                   state))}

   {:name "set-balance"
    :query '[:find ?balance
             :where 
             [?id :event/type :have-balance]
             [?id :payload ?balance]]
    :function (fn ([state balance]
                   (state/set-balance state balance)))}

   {:name "reset-state"
    :query '[:find ?id
             :where 
             [?id :event/type :reset-purchase]]
    :function (fn ([state _]
                   (events/send-event (:channel state) :reset-state :change-page {:page :login})
                   (state/reset state)))}

   {:name "reset-state-update-products"
    :query '[:find ?id
             :where 
             [?id :event/type :reset-purchase]]
    :function (fn ([state _]
                   (http/http-get (:channel state) "/products")
                   state))}

   {:name "keyboard-listener"
    :query '[:find ?character
             :where 
             [?id :event/type :character]
             [?id :payload ?character]]
    :function (fn ([state character]
                   (js/console.log (str character))
                   state))}

{:name "choose-money"
 :query '[:find ?amount
          :where
          [?id :event/type :money-chosen]
          [?id :payload ?amount]]
 :function (fn ([state amount]
                (state/choose-money state amount)))}

{:name "add-money"
 :query '[:find ?amount
          :where
          [?id :event/type :add-money]
          [?id :payload ?amount]]
 :function (fn ([state amount]
                (http/http-post (:channel state)
                                (str "/user/" (:user-id state) "/addmoney")
                                {:amount amount})
                state))}

{:name "set-message"
 :query '[:find ?message-data
          :where
          [?id :event/type :set-message]
          [?id :payload ?message-data]]
 :function (fn [state message-data]
             (state/set-message state (:message message-data) (:type message-data)))}
])
