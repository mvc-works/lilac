
(defmacro deflilac [comp-name args body]
 `(def ~comp-name {
    :lilac-type :component
    :name ~comp-name
    :args '[~@(vec args)]
    :fn (fn [~@args] ~body)
    }))

(println (macroexpand '(deflilac a [x y] (+ x y))))

(deflilac lilac-a [x y] (+ x y))

(println (pr-str lilac-a))

(println ((:fn lilac-a) 1 2))
