
(ns lilac.main (:require [lilac.core :refer [lilac-number deflilac validate-lilac]]))

(def demo-data 1)

(deflilac lilac-demo () (lilac-number nil))

(defn run-demo! [] (validate-lilac demo-data lilac-demo))

(defn main! [] (println "Started.") (run-demo!))

(defn reload! [] (.clear js/console) (println "Reloaded.") (run-demo!))
