
(ns lilac.core)

(defmacro deflilac [comp-name args body]
 `(defn ~comp-name [~@args] {
    :lilac-type :component
    :name (keyword '~comp-name)
    :args [~@args]
    :fn (fn [~@args] ~body)
    }))

(defmacro dev-check [data rule]
  `(when lilac.core/in-dev?
    (let [result# (lilac.core/validate-lilac ~data ~rule)]
      (when-not (:ok? result#)
        (js/console.error (:formatted-message result#)
          \newline
          (str "(dev-check " '~data " " '~rule ") , where props is:")
          (~'clj->js ~data))))))
