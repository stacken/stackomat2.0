(ns frontend.events
  (:require [cljs.core.async :refer [<! >! chan close! alts! timeout]]
            [datascript.core :as d])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn send-event 
  ([channel event]
   (go (>! channel event)))
  ([channel source event-type]
   (send-event channel {:source source :type event-type}))
  ([channel source event-type payload]
   (send-event channel {:source source :type event-type :payload payload})))

(defn make-channel []
  (chan 100))

(defn close-channel! [channel]
  (close! channel))

(defn- get-base-datom [event]
  (cond (or (= :http-response (:type event))
            (= :http-client-error (:type event))
            (= :http-server-error (:type event)))
        (merge {:url (:url (:payload event))}
               (:response (:payload event)))

        true
        event))

(defn- convert-event-to-datascript [event]
  (let [datom (merge (get-base-datom event)
                     {:db/id -1
                      :event/type (:type event)
                      :event/source (:source event)})
        conn (d/create-conn)]
    (d/transact! conn [datom])
    conn))

(defn- make-reduce-function [conn]
  (fn [state event-handler]
    (let [query (:query event-handler)
          query-ins (:query-ins event-handler)
          args (if query-ins
                 [query conn query-ins]
                 [query conn])
          query-results (apply d/q args)]
      (if (not (empty? query-results))
        (do (js/console.log (str "Running " (:name event-handler) " with data " (first query-results)))
            (apply (:function event-handler)
                   state 
                   (first query-results)))
        state))))

(defn listen [channel state eventset timeout-in-ms]
  (go-loop
    []
    (let [event (first (alts! [(timeout timeout-in-ms) channel]))]
      (if (nil? event)
        (send-event channel :timeout :reset-purchase)
        (let [conn (convert-event-to-datascript event)
              reduce-function (make-reduce-function @conn)]
          (js/console.log (str event))
          (swap! state #(reduce reduce-function % eventset)))))
    (recur)))
