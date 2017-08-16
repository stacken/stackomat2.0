(ns frontend.db
  (require [yesql.core :refer [defqueries]]))

(def db-spec {:classname "org.postgresql.Driver"
              :subprotocol "postgresql"
              :subname "//localhost:5432/fabian"
              :user "fabian"})

(defqueries "sql/queries.sql"
  {:connection db-spec})

(defn get-balance [params]
  (:balance (first (select-balance params))))

