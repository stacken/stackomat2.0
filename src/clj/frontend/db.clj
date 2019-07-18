(ns frontend.db
  (require [yesql.core :refer [defqueries]]
           [environ.core :refer [env]]))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname (env :stackomat-subname)
              :user (env :stackomat-user)
              :password (env :stackomat-password)})

(defqueries "sql/queries.sql"
  {:connection db-spec})

(defn get-balance [params]
  (:balance (first (select-balance params))))

