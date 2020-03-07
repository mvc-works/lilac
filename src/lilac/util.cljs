
(ns lilac.util )

(defn check-keys [message data xs]
  (let [valid-keys (set xs), real-keys (keys data)]
    (doseq [k real-keys]
      (when (not (contains? valid-keys k))
        (js/console.warn message "unexpected key" (pr-str k) ", expect" (pr-str valid-keys))))))

(defn preview-data [x]
  (cond
    (string? x) (pr-str x)
    (boolean? x) (str x)
    (number? x) (str x)
    (keyword? x) (str x)
    (symbol? x) (str "'" x)
    (map? x) "a map"
    (vector? x) "a vector"
    (set? x) "a set"
    (list? x) "a list"
    (nil? x) "nil"
    (seq? x) "a seq"
    :else (str "Unknown: " (subs (str x) 0 10))))

(def type-of-re (type #"x"))

(defn re? [x] (= type-of-re (type x)))
