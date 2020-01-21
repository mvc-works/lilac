
(ns lilac.core)

(defmacro deflilac [comp-name args body]
 `(def ~comp-name {
    :lilac-type :component
    :name ~comp-name
    :args '[~@(vec args)]
    :fn (fn [~@args] ~body)
    }))
