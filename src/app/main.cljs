
(ns app.main (:require [skir.client :as skir-client]))

(defn get-page! []
  (println "Start task...")
  (skir-client/fetch!
   "http://news.ycombinator.com/item?id=17533341"
   (fn [response] (println response))))

(defn main! [] (println "Started.") (get-page!))

(defn reload! [] (.clear js/console) (println "Reloaded.") (get-page!))
