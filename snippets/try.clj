
(defmacro deflilac [comp-name args body]
 `(defn ~comp-name [~@args] {
    :lilac-type :component
    :name (keyword '~comp-name)
    :args [~@args]
    :fn (fn [~@args] ~body)
    }))

(println (macroexpand '(deflilac a [x y] (+ x y))))

(deflilac lilac-a [x y] (+ x y))

(println (pr-str (lilac-a 3 4)))

(println ((:fn (lilac-a 3 4)) 1 2))
