
(ns lilac.core
  (:require-macros [lilac.core])
  (:require [lilac.util :refer [re? preview-data check-keys seq-equal seq-difference]]
            [clojure.string :as string]))

(declare validate-set)

(declare validate-record)

(declare core-methods)

(declare validate-and)

(declare validate-optional)

(declare validate-tuple)

(declare validate-map)

(declare validate-not)

(declare validate-or)

(declare validate-lilac)

(declare validate-list)

(declare validate-pick-type)

(declare validate-vector)

(declare validate-component)

(defonce *custom-methods (atom {}))

(defn and+
  ([items] (and+ items nil))
  ([items options]
   (assert (vector? items) "expects items of and+ in vector")
   {:lilac-type :and, :items items, :options options}))

(defn any+
  ([] (any+ nil))
  ([options]
   (check-keys "checking any+" options [:some?])
   {:lilac-type :any, :options options, :some? (:some? options)}))

(defn boolean+ ([] (boolean+ nil)) ([options] {:lilac-type :boolean}))

(defn format-message [acc result]
  (if (nil? result)
    acc
    (let [message (str (:message result) " at " (vec (remove symbol? (:coord result))))]
      (recur (str acc (if (some? acc) "\n" "") message) (:next result)))))

(def ok-result {:ok? true})

(defn validate-any [data rule coord]
  (let [coord (conj coord 'number), something? (:some? rule)]
    (if something?
      (if (some? data)
        ok-result
        {:ok? false,
         :data data,
         :rule rule,
         :coord coord,
         :message (or (get-in rule [:options :message])
                      (str "expects something, got " (preview-data data)))})
      ok-result)))

(defn validate-boolean [data rule coord]
  (if (boolean? data)
    ok-result
    {:ok? false,
     :data data,
     :rule rule,
     :coord (conj coord 'boolean),
     :message (or (get-in rule [:options :message])
                  (str "expects a boolean, got " (preview-data data)))}))

(defn validate-custom [data rule coord]
  (let [method (:fn rule), next-coord (conj coord 'custom), result (method data rule coord)]
    (if (:ok? result)
      result
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (or (:message result)
                    (get-in rule [:options :message])
                    "failed to validate with custom method")})))

(defn validate-enum [data rule coord]
  (let [coord (conj coord 'enum), items (:items rule)]
    (if (contains? items data)
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects value of " (pr-str items) ", got " (preview-data data)))})))

(defn validate-fn [data rule coord]
  (let [next-coord (conj coord 'fn)]
    (if (fn? data)
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a function, got " (preview-data data)))})))

(defn validate-is [data rule coord]
  (let [coord (conj coord 'is)]
    (if (= data (:item rule))
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str
                     "expects just "
                     (preview-data (:item rule))
                     ", got "
                     (preview-data data)))})))

(defn validate-keyword [data rule coord]
  (let [next-coord (conj coord 'keyword)]
    (if (keyword? data)
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a keyword, got " (preview-data data)))})))

(defn validate-nil [data rule coord]
  (let [next-coord (conj coord 'nil)]
    (if (nil? data)
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord next-coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a nil, got " (preview-data data)))})))

(defn validate-number [data rule coord]
  (let [coord (conj coord 'number), min-v (:min rule), max-v (:max rule)]
    (if (number? data)
      (if (and (if (some? min-v) (>= data min-v) true)
               (if (some? max-v) (<= data max-v) true))
        ok-result
        {:ok? false,
         :data data,
         :rule rule,
         :coord coord,
         :message (or (get-in rule [:options :message])
                      (str "expects number not in the range, got " (preview-data data)))})
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a number, got " (preview-data data)))})))

(defn validate-re [data rule coord]
  (let [coord (conj coord 're)]
    (if (re? data)
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a regular expression, got " (preview-data data)))})))

(defn validate-string [data rule coord]
  (let [coord (conj coord 'string), re (:re rule), nonblank? (:nonblank? rule)]
    (if (string? data)
      (cond
        (some? re)
          (if (re-matches re data)
            ok-result
            {:ok? false,
             :data data,
             :rule rule,
             :coord coord,
             :message (or (get-in rule [:options :message])
                          (str "expects a string in " re ", got " (preview-data data)))})
        (some? nonblank?)
          (if (and nonblank? (string/blank? data))
            {:ok? false,
             :data data,
             :rule rule,
             :coord coord,
             :message (or (get-in rule [:options :message])
                          (str "expects nonblank string , got " (preview-data data)))}
            ok-result)
        :else ok-result)
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expected a string, but got " (preview-data data)))})))

(defn validate-symbol [data rule coord]
  (let [coord (conj coord 'symbol)]
    (if (symbol? data)
      ok-result
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a symbol, got " (preview-data data)))})))

(defn validate-vector [data rule coord]
  (let [item-rule (:item rule), coord (conj coord 'vector), allow-seq? (:allow-seq? rule)]
    (if (or (vector? data) (if allow-seq? (seq? data) false))
      (loop [xs data, idx 0]
        (if (empty? xs)
          ok-result
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result) (recur (rest xs) (inc idx)) result))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a vector, got " (preview-data data)))})))

(defn validate-tuple [data rule coord]
  (let [items (:items rule)
        next-coord (conj coord 'tuple)
        in-list? (:in-list? rule)
        check-size? (:check-size? rule)
        check-values (fn []
                       (loop [xs items, ys data, idx 0]
                         (if (empty? xs)
                           (if check-size?
                             (if (and (empty? ys) (= (count items) (count data)))
                               {:ok? true}
                               {:ok? false,
                                :coord next-coord,
                                :rule rule,
                                :data ys,
                                :message (get-in
                                          rule
                                          [:options :message]
                                          (str
                                           "expects tuple of "
                                           (count items)
                                           " items, got "
                                           (count data)))})
                             ok-result)
                           (let [r0 (first xs)
                                 y0 (first ys)
                                 child-coord (conj next-coord idx)
                                 result (validate-lilac y0 r0 child-coord)]
                             (if (:ok? result)
                               (recur (rest xs) (rest ys) (inc idx))
                               {:ok? false,
                                :coord next-coord,
                                :rule rule,
                                :data y0,
                                :message (get-in
                                          rule
                                          [:options :message]
                                          "failed validating in \"tuple\""),
                                :next result})))))]
    (if in-list?
      (if (list? data)
        (check-values)
        {:ok? false,
         :data data,
         :rule rule,
         :coord coord,
         :message (str "expects a list for tuple, got " (preview-data data))})
      (if (vector? data)
        (check-values)
        {:ok? false,
         :data data,
         :rule rule,
         :coord coord,
         :message (str "expects a vector for tuple, got " (preview-data data))}))))

(defn validate-set [data rule coord]
  (let [item-rule (:item rule), coord (conj coord 'set)]
    (if (set? data)
      (loop [xs data, idx 0]
        (if (empty? xs)
          ok-result
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result) (recur (rest xs) (inc idx)) result))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a set, got " (preview-data data)))})))

(defn validate-record [data rule coord]
  (let [coord (conj coord 'record)
        pairs (:pairs rule)
        exact-keys? (:exact-keys? rule)
        check-keys? (:check-keys? rule)
        all-optional? (:all-optional? rule)
        default-message (get-in rule [:options :message])
        wanted-keys (keys pairs)
        existed-keys (if (map? data) (keys data))
        check-values (fn []
                       (loop [xs pairs]
                         (if (empty? xs)
                           ok-result
                           (let [[k0 r0] (first xs)
                                 child-coord (conj coord k0)
                                 v (get data k0)]
                             (if (and all-optional? (nil? v))
                               (recur (rest xs))
                               (let [result (validate-lilac v r0 child-coord)]
                                 (if (:ok? result) (recur (rest xs)) result)))))))]
    (if (not (map? data))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a record, got " (preview-data data)))}
      (cond
        exact-keys?
          (if (seq-equal existed-keys wanted-keys)
            (check-values)
            {:ok? false,
             :data data,
             :rule rule,
             :coord coord,
             :message (or default-message
                          (let [extra-keys (seq-difference existed-keys wanted-keys)
                                missing-keys (seq-difference wanted-keys existed-keys)]
                            (if (not (empty? extra-keys))
                              (str "unexpected record keys " extra-keys " for " wanted-keys)
                              (str "missing record keys " missing-keys " of " wanted-keys))))})
        check-keys?
          (let [extra-keys (seq-difference existed-keys wanted-keys)]
            (if (empty? extra-keys)
              (check-values)
              {:ok? false,
               :data data,
               :rule rule,
               :coord coord,
               :message (or default-message
                            (str "unexpected record keys " extra-keys " for " wanted-keys))}))
        :else (check-values)))))

(defn validate-pick-type [data rule coord]
  (let [dict (:dict rule)
        next-coord (conj coord 'pick-type)
        type-field (:type-field rule)
        data-type (get data type-field)]
    (if (nil? (get dict data-type))
      {:ok? false,
       :coord next-coord,
       :rule rule,
       :data data,
       :message (get-in
                 rule
                 [:options :message]
                 (str "found no matched type in pick-type: " data-type))}
      (let [next-rule (get dict data-type)
            result (validate-lilac data next-rule next-coord)]
        (if (:ok? result)
          result
          {:ok? false,
           :coord next-coord,
           :rule rule,
           :data data,
           :message (get-in rule [:options :message] (str "failed to match in pick-type")),
           :next result})))))

(defn validate-or [data rule coord]
  (let [items (:items rule), next-coord (conj coord 'or)]
    (loop [xs items, branches []]
      (if (empty? xs)
        {:ok? false,
         :coord next-coord,
         :rule rule,
         :data data,
         :message (get-in rule [:options :message] "found no matched case in \"or\""),
         :branches branches,
         :next (peek branches)}
        (let [r0 (first xs), result (validate-lilac data r0 next-coord)]
          (if (:ok? result) result (recur (rest xs) (conj branches result))))))))

(defn validate-optional [data rule coord]
  (let [item (:item rule), coord (conj coord 'optional)]
    (if (nil? data) ok-result (validate-lilac data item coord))))

(defn validate-not [data rule coord]
  (let [coord (conj coord 'not), item (:item rule), result (validate-lilac data item coord)]
    (if (:ok? result)
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (get-in rule [:options :message] "expects a inverted value in \"not\""),
       :next result}
      ok-result)))

(defn validate-map [data rule coord]
  (let [key-rule (:key-shape rule), item-rule (:item rule), coord (conj coord 'map)]
    (if (map? data)
      (loop [xs data]
        (if (empty? xs)
          ok-result
          (let [[k v] (first xs)
                child-coord (conj coord k)
                k-result (validate-lilac k key-rule child-coord)
                result (validate-lilac v item-rule child-coord)]
            (if (:ok? k-result) (if (:ok? result) (recur (rest xs)) result) k-result))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a map, got " (preview-data data)))})))

(defn validate-list [data rule coord]
  (let [item-rule (:item rule), coord (conj coord 'list), allow-seq? (:allow-seq? rule)]
    (if (or (list? data) (if allow-seq? (seq? data) false))
      (loop [xs data, idx 0]
        (if (empty? xs)
          ok-result
          (let [x0 (first xs)
                child-coord (conj coord idx)
                result (validate-lilac x0 item-rule child-coord)]
            (if (:ok? result) (recur (rest xs) (inc idx)) result))))
      {:ok? false,
       :data data,
       :rule rule,
       :coord coord,
       :message (or (get-in rule [:options :message])
                    (str "expects a list, got " (preview-data data)))})))

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
        ok-result
        (let [r0 (first xs), result (validate-lilac data r0 next-coord)]
          (if (:ok? result)
            (recur (rest xs))
            {:ok? false,
             :coord next-coord,
             :rule rule,
             :data data,
             :message (get-in rule [:options :message] "failed validating in \"and\""),
             :next result}))))))

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
   :record validate-record,
   :map validate-map,
   :list validate-list,
   :set validate-set,
   :not validate-not,
   :or validate-or,
   :and validate-and,
   :custom validate-custom,
   :component validate-component,
   :is validate-is,
   :optional validate-optional,
   :tuple validate-tuple,
   :any validate-any,
   :enum validate-enum,
   :pick-type validate-pick-type})

(defn custom+
  ([f] (custom+ f nil))
  ([f options] {:lilac-type :custom, :fn f, :options options}))

(defn enum+
  ([x] (enum+ x nil))
  ([items options]
   {:lilac-type :enum,
    :items (cond
      (set? items) items
      (vector? items) (set items)
      (list? items) (set items)
      :else (do (js/console.warn "Unknown items") items))}))

(defn fn+ ([] (fn+ nil)) ([options] {:lilac-type :fn, :options options}))

(def in-dev? (do ^boolean js/goog.DEBUG))

(defn is+ ([x] (is+ x nil)) ([x options] {:lilac-type :is, :item x}))

(defn keyword+ ([] (keyword+ nil)) ([options] {:lilac-type :keyword, :options options}))

(defn list+
  ([item] (list+ item nil))
  ([item options]
   (check-keys "checking list+" options [:allow-seq?])
   {:lilac-type :list, :item item, :options options, :allow-seq? (:allow-seq? options)}))

(defn map+
  ([key-shape item] (map+ key-shape item nil))
  ([key-shape item options]
   {:lilac-type :map, :key-shape key-shape, :item item, :options options}))

(defn nil+ ([] (nil+ {})) ([options] {:lilac-type :nil}))

(defn not+
  ([item] (not+ item nil))
  ([item options] {:lilac-type :not, :item item, :options options}))

(defn number+
  ([] (number+ nil))
  ([options]
   (check-keys "checking number+" options [:max :min])
   {:lilac-type :number, :max (:max options), :min (:min options), :options options}))

(defn optional+
  ([item] (optional+ item nil))
  ([item options] {:lilac-type :optional, :item item, :options options}))

(defn or+
  ([items] (or+ items nil))
  ([items options]
   (assert (vector? items) "expects items of or+ in vector")
   {:lilac-type :or, :items items, :options options}))

(defn pick-type+
  ([dict] (pick-type+ dict nil))
  ([dict options]
   (check-keys "checking pick-type+" options [:type-field])
   {:lilac-type :pick-type,
    :dict dict,
    :options options,
    :type-field (or (:type-field options) :type)}))

(defn re+ ([re] (re+ re nil)) ([re options] {:lilac-type :re, :re re, :options options}))

(defn record+
  ([pairs] (record+ pairs nil))
  ([pairs options]
   (check-keys "checking record+" options [:exact-keys? :check-keys? :all-optional?])
   {:lilac-type :record,
    :pairs pairs,
    :options options,
    :exact-keys? (:exact-keys? options),
    :check-keys? (:check-keys? options),
    :all-optional? (:all-optional? options)}))

(defn register-custom-rule! [type-name f]
  (assert (keyword? type-name) "expects type name in keyword")
  (assert (fn? f) "expects validation method in function")
  (println "registering validation rule" type-name)
  (swap! *custom-methods assoc type-name f))

(defn set+
  ([item] (set+ item nil))
  ([item options] {:lilac-type :set, :item item, :options options}))

(defn string+
  ([] (string+ nil))
  ([options]
   (check-keys "checking string+" options [:nonblank? :re])
   {:lilac-type :string,
    :re (:re options),
    :nonblank? (:nonblank? options),
    :options options}))

(defn symbol+ ([] (symbol+ nil)) ([options] {:lilac-type :symbol}))

(defn tuple+
  ([items] (tuple+ items nil))
  ([items options]
   (assert (vector? items) "expects items of tuple+ in vector")
   (check-keys "checking tuple+" options [:in-list? :check-size?])
   {:lilac-type :tuple,
    :items items,
    :options options,
    :in-list? (:in-list? options),
    :check-size? (:check-size? options)}))

(defn vector+
  ([item] (vector+ item nil))
  ([item options]
   (check-keys "checking vector+" options [:allow-seq?])
   {:lilac-type :vector, :item item, :options options, :allow-seq? (:allow-seq? options)}))
