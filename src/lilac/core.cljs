
(ns lilac.core (:require-macros [lilac.core]) (:require [lilac.util :refer [re?]]))

(declare validate-set)

(declare core-methods)

(declare validate-and)

(declare validate-map)

(declare validate-not)

(declare validate-or)

(declare validate-lilac)

(declare validate-list)

(declare validate-vector)

(declare validate-component)

(defn validate-boolean [data rule coord]
  (if (boolean? data)
    {:ok? true}
    {:ok? false, :data data, :rule rule, :coord (conj coord :or), :message "Not a boolean"}))

(defn validate-custom [data rule coord]
  (let [method (:fn rule), next-coord (conj coord :custom), result (method data rule coord)]
    (if (:ok? result)
      result
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message "Failed to validate with custom method"})))

(defn validate-fn [data rule coord]
  (let [next-coord (conj coord :fn)]
    (if (fn? data)
      {:ok? true}
      {:ok? false, :data data, :rule rule, :coord next-coord, :message "Not a function"})))

(defn validate-is [data rule coord]
  (let [coord (conj coord :is)]
    (if (= data (:item rule))
      {:ok? true}
      {:ok? false, :data data, :rule rule, :coord coord, :message "Values not equal"})))

(defn validate-keyword [data rule coord]
  (let [next-coord (conj coord :keyword)]
    (if (keyword? data)
      {:ok? true}
      {:ok? false, :data data, :rule rule, :coord next-coord, :message "Not a keyword"})))

(defn validate-nil [data rule coord]
  (let [next-coord (conj coord :nil)]
    (if (nil? data)
      {:ok? true}
      {:ok? false, :data data, :rule rule, :coord next-coord, :message "Expects a nil"})))

(defn validate-number [data rule coord]
  (let [coord (conj coord :number), min-v (:min rule), max-v (:max rule)]
    (if (number? data)
      (if (and (if (some? min-v) (>= data min-v) true)
               (if (some? max-v) (<= data max-v) true))
        {:ok? true}
        {:ok? false, :data data, :rule rule, :coord coord, :message "Number not in range"})
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a number"})))

(defn validate-re [data rule coord]
  (let [coord (conj coord :re)]
    (if (re? data)
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message "Not a regular expression"})))

(defn validate-string [data rule coord]
  (let [coord (conj coord :string), re (:re rule)]
    (if (string? data)
      (if (some? re)
        (if (re-matches re data)
          {:ok? true}
          {:ok? false,
           :data data,
           :rule rule,
           :coord coord,
           :message "Not passing regular expression"})
        {:ok? true})
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a string"})))

(defn validate-symbol [data rule coord]
  (let [coord (conj coord :symbol)]
    (if (symbol? data)
      {:ok? true}
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a symbol"})))

(defn validate-vector [data rule coord]
  (let [item-rule (:item rule), coord (conj coord :list)]
    (if (vector? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          {:ok? true}
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result) (recur (rest xs) (inc idx)) result))))
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a list"})))

(defn validate-set [data rule coord]
  (let [item-rule (:item rule), coord (conj coord :list)]
    (if (set? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          {:ok? true}
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result) (recur (rest xs) (inc idx)) result))))
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a list"})))

(defn validate-or [data rule coord]
  (let [items (:items rule), next-coord (conj coord :or)]
    (loop [xs items]
      (if (empty? xs)
        {:ok? false,
         :coord next-coord,
         :rule rule,
         :data data,
         :message "Found no match in or"}
        (let [r0 (first xs), result (validate-lilac data r0 next-coord)]
          (if (:ok? result) result (recur (rest xs))))))))

(defn validate-not [data rule coord]
  (let [coord (conj coord :not), item (:item rule), result (validate-lilac data item coord)]
    (if (:ok? result)
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message "Expects a inverted value in \"not\""}
      {:ok? true})))

(defn validate-map [data rule coord]
  (let [coord (conj coord :map), pairs (:pairs rule)]
    (if (map? data)
      (loop [xs pairs]
        (if (empty? xs)
          {:ok? true}
          (let [[k0 r0] (first xs)
                child-coord (conj coord k0)
                result (validate-lilac (get data k0) r0 child-coord)]
            (if (:ok? result)
              (recur (rest xs))
              {:ok? false,
               :data (get data k0),
               :rule r0,
               :coord child-coord,
               :message (str "field" (str k0) "not validated")}))))
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a map"})))

(defn validate-list [data rule coord]
  (let [item-rule (:item rule), coord (conj coord :list)]
    (if (list? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          {:ok? true}
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result) (recur (rest xs) (inc idx)) result))))
      {:ok? false, :data data, :rule rule, :coord coord, :message "Not a list"})))

(defn validate-lilac
  ([data rule] (validate-lilac data rule []))
  ([data rule coord]
   (comment println "got" rule)
   (let [kind (:lilac-type rule), method (get core-methods kind)]
     (when (nil? method) (println "Unknown method:" kind "of" rule) (.exit js/process 1))
     (comment println "calling method for" kind method)
     (method data rule coord))))

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
             :coord next-coord,
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
   :component validate-component,
   :is validate-is})

(defn lilac-and [& items] {:lilac-type :and, :items items})

(defn lilac-boolean [] {:lilac-type :boolean})

(defn lilac-custom [f options] {:lilac-type :custom, :fn f, :options options})

(defn lilac-fn [f options] {:lilac-type :fn, :fn f, :options options})

(defn lilac-is [x] {:lilac-type :is, :item x})

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
