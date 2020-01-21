
(ns lilac.main
  (:require [lilac.core :refer [lilac-number lilac-or deflilac validate-lilac lilac-string]]
            ["fs" :as fs]
            ["path" :as path]
            [cljs.reader :refer [read-string]]))

(deflilac lilac-demo () (lilac-or (lilac-number nil) (lilac-string nil)))

(defn run-demo! []
  (let [data (read-string (fs/readFileSync (path/join js/__dirname "config.edn") "utf8"))
        result (validate-lilac data lilac-demo [])]
    (println)
    (println "Result" result)))

(defn main! [] (println "Started.") (run-demo!))

(defn reload! [] (.clear js/console) (println "Reloaded.") (run-demo!))
