(ns frontend.http
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [frontend.state :as state]
            [frontend.events :as events]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(defn- is-ok [status]
  (and (>= 200 status) (<= 299 status)))

(defn- is-client-error [status]
  (and (>= 400 status) (<= 499 status)))

(defn- is-server-error [status]
  (and (>= 500 status) (<= 599 status)))

(defn- get-response-type [status]
  (cond (is-client-error status) :http-client-error
        (is-server-error status) :http-server-error
        ;; 1xx and 3xx are currently out of scope
        true :http-response))

(defn make-response-event [url response]
  (events/make-event :http 
                     (get-response-type (:status response))
                     {:url url :response response}))

(defn http-get [channel url]
  (go (events/send-event
        channel 
        (make-response-event url (<! (http/get url))))))

(defn http-post [channel url params & callback-events]
  (go (let [token-response (<! (http/get "/token"))
            csrf-token (:body token-response)]
        (go (let [result (<! (http/post url {:json-params params
                                             :headers ["X-CSRF-TOKEN" csrf-token]}))
                  response-event (make-response-event url result)
                  events-to-send (concat [response-event] callback-events)]
              (reduce (fn [count event]
                        (events/send-event channel event)
                        (+ count 1))
                      0 
                      events-to-send))))))
