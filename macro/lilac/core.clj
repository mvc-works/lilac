
(ns lilac.core)

(defmacro deflilac [comp-name args body]
 `(defn ~comp-name [~@args] {
    :lilac-type :component
    :name (keyword '~comp-name)
    :args [~@args]
    :fn (fn [~@args] ~body)
    }))
