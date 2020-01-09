
(ns lilac.core )

(defn lilac-fn [f] [{:type :fn, :fn f}])

(defn lilac-vector [validator]
  [{:type :type, :fn vector?}
   {:type :item, :fn validator}
   {:type :or, :cases [{:type :fn, :fn inc?} {:type :lilac, :fn lilac-vector}]}])

(defn validate-lilac [data rule] (println "TODO"))
