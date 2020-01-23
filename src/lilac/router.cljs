
(ns lilac.router
  (:require [lilac.core
             :refer
             [validate-lilac
              number+
              string+
              keyword+
              boolean+
              nil+
              vector+
              list+
              map+
              set+
              deflilac
              or+
              and+
              not+
              custom+
              is+]]))

(deflilac lilac-method+ () (or+ [(nil+) (map+ {:type (is+ :file), :file (string+)})]))

(deflilac
 lilac-router-path+
 ()
 (map+
  {:path (string+),
   :get (lilac-method+),
   :post (lilac-method+),
   :put (lilac-method+),
   :delete (lilac-method+),
   :next (or+ [(nil+) (vector+ (lilac-router-path+))])}))

(deflilac lilac-router+ () (map+ {:port (number+), :routes (vector+ (lilac-router-path+))}))

(def router-data
  {:port 7800,
   :routes [{:path "home", :get {:type :file, :file "home.json"}}
            {:path "plants/:plant-id",
             :get {:type :file, :file "plant-default.json"},
             :post {:type :file, :file "ok.json"},
             :next [{:path "overview", :get {:type :file, :file "overview.json"}}
                    {:path "materials/:material-id",
                     :get {:type :file, :file "materials.json"},
                     :next [{:path "events",
                             :get {:type :file, :file "events.json"},
                             :delete {:code 202, :type :file, :file "ok.json"}}]}]}]})