
(ns lilac.util )

(defn check-keys [message data xs]
  (let [real-keys (keys data)]
    (doseq [k real-keys]
      (when (not (some (fn [x] (= k x)) xs))
        (js/console.warn message "unexpected key" (pr-str k) ", expect" (pr-str xs))))))

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

(defn seq-difference [xs ys] (->> xs (remove (fn [x] (->> ys (some (fn [y] (= x y))))))))

(defn seq-equal [xs ys]
  (and (->> xs (every? (fn [x] (->> ys (some (fn [y] (= x y)))))))
       (->> ys (every? (fn [y] (->> xs (some (fn [x] (= x y)))))))))
