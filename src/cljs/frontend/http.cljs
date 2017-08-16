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

(defn- send-response-event [channel url response]
  (events/send-event channel 
                     :http
                     (get-response-type (:status response))
                     {:url url :response response}))

(defn http-get [channel url]
  (go (send-response-event channel url (<! (http/get url)))))

(defn http-post [channel url params]
  (go (let [token-response (<! (http/get "/token"))
            csrf-token (:body token-response)]
        (go (let [result (<! (http/post url {:json-params params
                                             :headers ["X-CSRF-TOKEN" csrf-token]}))]
              (send-response-event channel url result))))))
