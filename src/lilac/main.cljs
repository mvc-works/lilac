
(ns lilac.main )

(defn main! [] (println "Started."))

(defn reload! [] (.clear js/console) (println "Reloaded."))
