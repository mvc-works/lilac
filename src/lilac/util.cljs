
(ns lilac.util )

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
