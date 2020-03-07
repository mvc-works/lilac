
(ns lilac.router
  (:require [lilac.core
             :refer
             [validate-lilac
              deflilac
              optional+
              keyword+
              boolean+
              number+
              string+
              custom+
              vector+
              list+
              record+
              not+
              and+
              set+
              nil+
              or+
              is+]]))

(deflilac
 lilac-method+
 ()
 (optional+
  (record+
   {:code (optional+ (number+)), :type (is+ :file), :file (string+)}
   {:check-keys? true})))

(deflilac
 lilac-router-path+
 ()
 (record+
  {:path (string+),
   :get (lilac-method+),
   :post (lilac-method+),
   :put (lilac-method+),
   :delete (lilac-method+),
   :next (optional+ (vector+ (lilac-router-path+)))}
  {:check-keys? true}))

(deflilac
 lilac-router+
 ()
 (record+ {:port (number+), :routes (vector+ (lilac-router-path+))} {:exact-keys? true}))

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
