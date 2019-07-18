(ns frontend.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [frontend.middleware :refer [wrap-middleware]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.util.response :refer [response content-type status]]
            [digest :refer [sha-256]]
            [frontend.db :as db]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "Loading..."]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   [:link {:rel "stylesheet" 
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
           :integrity "sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
           :crossorigin "anonymous"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn make-response 
  ([body] (make-response 200 body))
  ([status-code body] (-> body
                          response
                          (status status-code)
                          (content-type "application/json"))))

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn list-products []
  (make-response (db/list-products)))


(defn handle-pay [request]
  (let [user-id (sha-256 (get-in request [:params :user-id]))
        amount (get-in request [:body :amount])
        balance (db/get-balance {:id user-id})]
    (if (and (not (nil? balance)) ; check the user exists
             (>= balance amount)); and can afford
      (do (db/pay! {:id user-id :amount amount})
          (let [new-balance (db/get-balance {:id user-id})]
            (make-response {:amount amount :balance new-balance})))
      (make-response 403 {:message "Du har inte råd."}))))

(defn handle-add-money [request]
  (let [user-id (sha-256 (get-in request [:params :user-id]))
        amount (get-in request [:body :amount])]
    (let [user (db/select-user {:id user-id})]
      (if (empty? user)
        (db/add-user! {:id user-id :balance 0}))
      (db/add-money! {:id user-id :amount amount})
      (make-response {:amount amount 
                      :balance (db/get-balance {:id user-id})}))))

(defn get-balance [user-id]
  (let [balance (db/get-balance {:id (sha-256 user-id)})]
    (make-response {:balance balance})))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/account" [] (loading-page))
  (GET "/token" [] (str *anti-forgery-token*))
  (GET "/products" [] (list-products))
  (GET "/user/:user-id/balance" [user-id] (get-balance user-id))
  (POST "/user/:user-id/addmoney" [user-id amount] handle-add-money)
  (POST "/user/:user-id/pay" [user-id amount] handle-pay)
  (resources "/")
  (not-found "Not Found"))

(def app (-> routes
             wrap-middleware
             (wrap-json-body {:keywords? true :bigdecimals? true})
             wrap-json-response))
