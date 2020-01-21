
(ns lilac.core)

(defmacro deflilac [comp-name args body]
 `(def ~comp-name {
    :lilac-type :component
    :name (keyword '~comp-name)
    :args '[~@(vec args)]
    :fn (fn [~@args] ~body)
    }))
