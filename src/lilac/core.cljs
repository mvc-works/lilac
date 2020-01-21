
(ns lilac.core (:require-macros [lilac.core]))

(declare core-methods)

(declare validate-and)

(declare validate-or)

(declare validate-lilac)

(declare validate-component)

(defn validate-boolean [] )

(defn validate-custom [] )

(defn validate-fn [] )

(defn validate-keyword [] )

(defn validate-list [] )

(defn validate-map [] )

(defn validate-nil [] )

(defn validate-not [] )

(defn validate-number [] )

(defn validate-re [] )

(defn validate-set [] )

(defn validate-string [] )

(defn validate-symbol [] )

(defn validate-vector [] )

(defn validate-or [data rule coord]
  (let [items (:items rule), next-coord (conj coord :or)]
    (loop [xs items]
      (if (empty? xs)
        {:ok? false, :coord coord, :rule rule, :data data, :message "Found no match in or"}
        (let [r0 (first xs), result (validate-lilac data r0 next-coord)]
          (if (:ok? result) result (recur (rest xs))))))))

(defn validate-lilac [data rule coord]
  (println "got" rule)
  (let [kind (:lilac-type rule), method (get core-methods kind)]
    (when (nil? method) (println "Unknown method:" kind "of" rule) (.exit js/process 1))
    (println "calling method for" kind method)
    (method data rule coord)))

(defn validate-component [data rule coord]
  (let [lazy-fn (:fn rule)
        next-coord (conj coord (:name rule))
        next-rule (apply lazy-fn (:args rule))]
    (validate-lilac data next-rule next-coord)))

(defn validate-and [data rule coord]
  (let [items (:items rule), next-coord (conj coord :and)]
    (loop [xs items]
      (if (empty? xs)
        {:ok? true}
        (let [r0 (first xs), result (validate-lilac data r0 next-coord)]
          (if (:ok? result)
            (recur (rest xs))
            {:ok? false,
             :coord coord,
             :rule rule,
             :data data,
             :message "No more candidates"}))))))

(def core-methods
  {:boolean validate-boolean,
   :string validate-string,
   :nil validate-nil,
   :fn validate-fn,
   :keyword validate-keyword,
   :symbol validate-symbol,
   :number validate-number,
   :re validate-re,
   :vector validate-vector,
   :map validate-map,
   :list validate-list,
   :set validate-set,
   :not validate-not,
   :or validate-or,
   :and validate-and,
   :custom validate-custom,
   :component validate-component})

(defn lilac-and [& items] {:lilac-type :and, :items items})

(defn lilac-boolean [options] {:lilac-type :boolean, :options options})

(defn lilac-custom [f options] {:lilac-type :custom, :fn f, :options options})

(defn lilac-fn [f options] {:lilac-type :fn, :fn f, :options options})

(defn lilac-keyword [options] {:lilac-type :keyword, :options options})

(defn lilac-list [item options] {:lilac-type :list, :item item, :options options})

(defn lilac-map [pairs options] {:lilac-type :map, :pairs pairs, :options options})

(defn lilac-nil [] {:lilac-type :nil})

(defn lilac-not [item] {:lilac-type :not, :item item})

(defn lilac-number [options]
  {:lilac-type :number, :max (:max options), :min (:min options), :options options})

(defn lilac-or [& items] {:lilac-type :or, :items items})

(defn lilac-re [options] {:lilac-type :re, :options options})

(defn lilac-set [item options] {:lilac-type :set, :item item, :options options})

(defn lilac-string [options] {:lilac-type :string, :re (:re options), :options options})

(defn lilac-symbol [options] {:lilac-type :symbol})

(defn lilac-vector [item options] {:lilac-type :vector, :item item, :options options})
