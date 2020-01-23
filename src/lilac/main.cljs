
(ns lilac.main
  (:require [lilac.core
             :refer
             [number+ or+ deflilac validate-lilac string+ map+ not+ nil+ vector+]]
            [cljs.reader :refer [read-string]]
            [lilac.router :refer [router-data lilac-router+]]))

(defn run-demo! []
  (let [result (validate-lilac router-data (lilac-router+))]
    (if (:ok? result) (println "Passed validation!") (println (:formatted-message result)))))

(defn main! [] (println "Started.") (run-demo!))

(defn reload! [] (.clear js/console) (println "Reloaded.") (run-demo!))
