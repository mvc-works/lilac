
(ns lilac.core (:require-macros [lilac.core]) (:require [lilac.util :refer [re?]]))

(declare validate-set)

(declare core-methods)

(declare validate-and)

(declare validate-optional)

(declare validate-map)

(declare validate-not)

(declare validate-or)

(declare validate-lilac)

(declare validate-list)

(declare validate-vector)

(declare validate-component)

(defonce *custom-methods (atom {}))

(defn and+
  ([items] (and+ items nil))
  ([items options] {:lilac-type :and, :items items, :options options}))

(defn boolean+ ([] (boolean+ nil)) ([options] {:lilac-type :boolean}))

(defn format-message [acc result]
  (if (nil? result)
    acc
    (let [message (str (remove symbol? (:coord result)) " - " (:message result))]
      (recur (str acc (if (some? acc) "\n" "") message) (:next result)))))

(defn validate-boolean [data rule coord]
  (if (boolean? data)
    {:ok? true}
    {:ok? false,
     :data data,
     :rule rule,
     :coord (conj coord 'boolean),
     :message (get-in rule [:options :message] "Not a boolean")}))

(defn validate-custom [data rule coord]
  (let [method (:fn rule), next-coord (conj coord 'custom), result (method data rule coord)]
    (if (:ok? result)
      result
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (get-in rule [:options :message] "Failed to validate with custom method")})))

(defn validate-fn [data rule coord]
  (let [next-coord (conj coord 'fn)]
    (if (fn? data)
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (get-in rule [:options :message] "Not a function")})))

(defn validate-is [data rule coord]
  (let [coord (conj coord 'is)]
    (if (= data (:item rule))
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Values not equal")})))

(defn validate-keyword [data rule coord]
  (let [next-coord (conj coord 'keyword)]
    (if (keyword? data)
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (get-in rule [:options :message] "Not a keyword")})))

(defn validate-nil [data rule coord]
  (let [next-coord (conj coord 'nil)]
    (if (nil? data)
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (get-in rule [:options :message] "Expects a nil")})))

(defn validate-number [data rule coord]
  (let [coord (conj coord 'number), min-v (:min rule), max-v (:max rule)]
    (if (number? data)
      (if (and (if (some? min-v) (>= data min-v) true)
               (if (some? max-v) (<= data max-v) true))
        {:ok? true}
        {:ok? false,
         :data data,
         :rule rule,
         :coord coord,
         :message (get-in rule [:options :message] "Number not in range")})
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a number")})))

(defn validate-re [data rule coord]
  (let [coord (conj coord 're)]
    (if (re? data)
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a regular expression")})))

(defn validate-string [data rule coord]
  (let [coord (conj coord 'string), re (:re rule)]
    (if (string? data)
      (if (some? re)
        (if (re-matches re data)
          {:ok? true}
          {:ok? false,
           :data data,
           :rule rule,
           :coord coord,
           :message (get-in rule [:options :message] "Not passing regular expression")})
        {:ok? true})
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a string")})))

(defn validate-symbol [data rule coord]
  (let [coord (conj coord 'symbol)]
    (if (symbol? data)
      {:ok? true}
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a symbol")})))

(defn validate-vector [data rule coord]
  (let [item-rule (:item rule), coord (conj coord 'vector)]
    (if (vector? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          {:ok? true}
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result)
              (recur (rest xs) (inc idx))
              {:ok? false,
               :data data,
               :rule rule,
               :coord coord,
               :message (get-in rule [:options :message] "Invalid child in vector"),
               :next result}))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a vector")})))

(defn validate-set [data rule coord]
  (let [item-rule (:item rule), coord (conj coord 'set)]
    (if (set? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          {:ok? true}
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result)
              (recur (rest xs) (inc idx))
              {:ok? false,
               :data data,
               :rule rule,
               :coord coord,
               :message (get-in rule [:options :message] "Invalid item in set"),
               :next result}))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a list")})))

(defn validate-or [data rule coord]
  (let [items (:items rule), next-coord (conj coord 'or)]
    (loop [xs items]
      (if (empty? xs)
        {:ok? false,
         :coord next-coord,
         :rule rule,
         :data data,
         :message (get-in rule [:options :message] "Found no match in or")}
        (let [r0 (first xs), result (validate-lilac data r0 next-coord)]
          (if (:ok? result) result (recur (rest xs))))))))

(defn validate-optional [data rule coord]
  (let [item (:item rule), coord (conj coord 'optional)]
    (if (nil? data) {:ok? true} (validate-lilac data item coord))))

(defn validate-not [data rule coord]
  (let [coord (conj coord 'not), item (:item rule), result (validate-lilac data item coord)]
    (if (:ok? result)
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Expects a inverted value in \"not\""),
       :next result}
      {:ok? true})))

(defn validate-map [data rule coord]
  (let [coord (conj coord 'map), pairs (:pairs rule)]
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
               :message (str "field " (str k0) " not validated"),
               :next result}))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a map")})))

(defn validate-list [data rule coord]
  (let [item-rule (:item rule), coord (conj coord 'list)]
    (if (list? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          {:ok? true}
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result)
              (recur (rest xs) (inc idx))
              {:ok? false,
               :data data,
               :rule rule,
               :coord coord,
               :message (get-in rule [:options :message] "Invalid field in map"),
               :next result}))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "Not a list")})))

(defn validate-lilac
  ([data rule] (validate-lilac data rule []))
  ([data rule coord]
   (comment println "got" rule)
   (let [kind (:lilac-type rule)
         method (get core-methods kind)
         user-method (get @*custom-methods kind)
         result (cond
                  (fn? method)
                    (do
                     (comment println "calling method for" kind method)
                     (method data rule coord))
                  (fn? user-method)
                    (do
                     (comment println "calling method for" kind method)
                     (user-method data rule coord))
                  :else (println "Unknown method:" kind "of" rule))]
     (if (:ok? result) result (assoc result :formatted-message (format-message nil result))))))

(defn validate-component [data rule coord]
  (let [lazy-fn (:fn rule)
        next-coord (conj coord (symbol (name (:name rule))))
        next-rule (apply lazy-fn (:args rule))]
    (validate-lilac data next-rule next-coord)))

(defn validate-and [data rule coord]
  (let [items (:items rule), next-coord (conj coord 'and)]
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
             :message (get-in rule [:options :message] "No more candidates")}))))))

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
   :is validate-is,
   :optional validate-optional})

(defn custom+
  ([f] (custom+ f nil))
  ([f options] {:lilac-type :custom, :fn f, :options options}))

(defn fn+ ([] (fn+ nil)) ([options] {:lilac-type :fn, :options options}))

(defn is+ ([x] (is+ x nil)) ([x options] {:lilac-type :is, :item x}))

(defn keyword+ ([] (keyword+ nil)) ([options] {:lilac-type :keyword, :options options}))

(defn list+
  ([item] (list+ item nil))
  ([item options] {:lilac-type :list, :item item, :options options}))

(defn map+
  ([pairs] (map+ pairs nil))
  ([pairs options] {:lilac-type :map, :pairs pairs, :options options}))

(defn nil+ ([] (nil+ {})) ([options] {:lilac-type :nil}))

(defn not+
  ([item] (not+ item nil))
  ([item options] {:lilac-type :not, :item item, :options options}))

(defn number+
  ([] (number+ nil))
  ([options]
   {:lilac-type :number, :max (:max options), :min (:min options), :options options}))

(defn optional+
  ([item] (optional+ item nil))
  ([item options] {:lilac-type :optional, :item item, :options options}))

(defn or+
  ([items] (or+ items nil))
  ([items options] {:lilac-type :or, :items items, :options options}))

(defn re+ ([re] (re+ re nil)) ([re options] {:lilac-type :re, :re re, :options options}))

(defn set+
  ([item] (set+ item nil))
  ([item options] {:lilac-type :set, :item item, :options options}))

(defn string+
  ([] (string+ nil))
  ([options] {:lilac-type :string, :re (:re options), :options options}))

(defn symbol+ ([] (symbol+ nil)) ([options] {:lilac-type :symbol}))

(defn vector+
  ([item] (vector+ item nil))
  ([item options] {:lilac-type :vector, :item item, :options options}))
