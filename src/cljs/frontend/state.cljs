(ns frontend.state
  (:require [frontend.events :as events]))

(defn make-state []
  {:all-products []
   :chosen-products []
   :key-counter 0
   :user-id ""
   :balance nil
   :money-to-add 0
   :message {:message "" :type :info}
   :channel (events/make-channel)})

(defn choose-product [state product]
  (let [key-counter (inc (:key-counter state))
        product (assoc product :key key-counter)]
    (-> state
        (assoc :chosen-products (conj (:chosen-products state) product))
        (assoc :key-counter key-counter))))

(defn remove-product [state product]
  (assoc state :chosen-products (remove #(= % product) 
                                        (:chosen-products state))))

(defn remove-all-chosen-products [state]
  (assoc state :chosen-products []))

(defn set-user-id [state user-id]
  (assoc state :user-id user-id))

(defn set-balance [state balance]
  (assoc state :balance balance))

(defn set-money-to-add [state amount]
  (assoc state :money-to-add amount))

(defn set-products [state products]
  (assoc state :all-products products))

(defn sum-of-chosen-products [state]
  (reduce + (map :price (:chosen-products state))))

(defn choose-money [state amount]
  (set-money-to-add state (+ amount (:money-to-add state))))

(defn set-message [state message type]
  (assoc state :message {:message message :type type}))

(defn reset [state]
  (-> state
      (remove-all-chosen-products)
      (set-money-to-add 0)
      (set-balance nil)
      (set-user-id "")
      (set-message "" :info)))

