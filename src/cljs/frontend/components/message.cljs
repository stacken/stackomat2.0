(ns frontend.components.message)

(defn message [state]
  (let [text (get-in [:message :text] @state)
        type (get-in [:message :type] @state)]
    (if (not (empty? text))
      (cond (= :success type)
            [:p {:class "text-success"} text]

            (= :error type)
            [:p {:class "text-danger"} text]

            true
            [:p {:class "text-muted"} text]))))
