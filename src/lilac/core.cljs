
(ns lilac.core (:require-macros [lilac.core]))

(def lilac-boolean {:lilac-type :boolean})

(defn lilac-fn [message f] {:lilac-type :fn, :fn f, :message message})

(defn lilac-number [options] {:lilac-type :number, :custom-fn (:custom-fn options)})

(defn lilac-vector [child-rule options]
  {:lilac-type :vector,
   :item child-rule,
   :size (:size options),
   :custom-fn (:custom-fn options)})

(defn validate-lilac [data rule] (println "TODO" data rule))
