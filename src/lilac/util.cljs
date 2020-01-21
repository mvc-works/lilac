
(ns lilac.util )

(def type-of-re (type #"x"))

(defn re? [x] (= type-of-re (type x)))
