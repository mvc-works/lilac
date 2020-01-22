
(ns lilac.test
  (:require [cljs.test :refer [deftest is testing run-tests]]
            [lilac.core
             :refer
             [validate-lilac
              lilac-number
              lilac-string
              lilac-keyword
              lilac-boolean
              lilac-nil
              lilac-vector
              lilac-list
              lilac-map
              lilac-set
              deflilac
              lilac-or
              lilac-and
              lilac-not
              lilac-custom
              lilac-is]]))

(defn =ok [x obj] (= x (:ok? obj)))

(deflilac lilac-good-number (n) (lilac-number {:min n}))

(deflilac
 lilac-method
 ()
 (lilac-or (lilac-nil) (lilac-map {:type (lilac-is :file), :file (lilac-string nil)} nil)))

(deflilac
 lilac-router-path
 ()
 (lilac-map
  {:path (lilac-string nil),
   :get (lilac-method),
   :post (lilac-method),
   :put (lilac-method),
   :delete (lilac-method),
   :next (lilac-or (lilac-nil) (lilac-vector (lilac-router-path) nil))}
  nil))

(deflilac
 lilac-router
 ()
 (lilac-map {:port (lilac-number nil), :routes (lilac-vector (lilac-router-path) nil)} nil))

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

(deftest
 test-and
 (testing
  "and number"
  (is (=ok true (validate-lilac 10 (lilac-and (lilac-number nil) (lilac-number {:min 0}))))))
 (testing
  "string not number"
  (is (=ok false (validate-lilac 10 (lilac-and (lilac-number nil) (lilac-string nil)))))))

(deftest
 test-boolean
 (testing "true is boolean" (is (=ok true (validate-lilac true (lilac-boolean)))))
 (testing "false is boolean" (is (=ok true (validate-lilac false (lilac-boolean)))))
 (testing "nil is no a boolean" (is (=ok false (validate-lilac nil (lilac-boolean)))))
 (testing "string is no a boolean" (is (=ok false (validate-lilac "x" (lilac-boolean))))))

(deftest
 test-component-args
 (testing "number 10 > 8" (is (=ok true (validate-lilac 10 (lilac-good-number 8)))))
 (testing "number 10 not > 18" (is (=ok false (validate-lilac 10 (lilac-good-number 18))))))

(deftest
 test-list
 (testing
  "a list of boolean"
  (is (=ok true (validate-lilac (list true true false) (lilac-list (lilac-boolean) nil)))))
 (testing
  "a empty list"
  (is (=ok true (validate-lilac (list) (lilac-list (lilac-boolean) nil)))))
 (testing
  "nil is not a list"
  (is (=ok false (validate-lilac nil (lilac-list (lilac-boolean) nil)))))
 (testing
  "a list of string is not list of boolean"
  (is (=ok false (validate-lilac (list "true" "false") (lilac-list (lilac-boolean) nil)))))
 (testing
  "vector is not a empty vector"
  (is (=ok false (validate-lilac [] (lilac-list (lilac-boolean) nil)))))
 (testing
  "boolean is not a empty vector"
  (is (=ok false (validate-lilac false (lilac-list (lilac-boolean) nil))))))

(deftest
 test-map
 (testing "an empty map" (is (=ok true (validate-lilac {} (lilac-map [] nil)))))
 (testing
  "an map of numbers"
  (is
   (=ok
    true
    (validate-lilac
     {1 100, 2 200}
     (lilac-map {1 (lilac-number nil), 2 (lilac-number nil)} nil)))))
 (testing
  "an map of numbers of not keyword/number"
  (is
   (=ok
    false
    (validate-lilac
     {:a 100, :b 200}
     (lilac-map {1 (lilac-number nil), 2 (lilac-number nil)} nil)))))
 (testing
  "an map of number and vector/string"
  (is
   (=ok
    true
    (validate-lilac
     {:a 100, :b ["red" "blue"]}
     (lilac-map {:a (lilac-number nil), :b (lilac-vector (lilac-string nil) nil)} nil))))))

(deftest
 test-nil
 (testing "a nil" (is (=ok true (validate-lilac nil (lilac-nil)))))
 (testing "string not nil" (is (=ok false (validate-lilac "x" (lilac-nil))))))

(deftest
 test-number
 (testing "a number" (is (=ok true (validate-lilac 1 (lilac-number nil)))))
 (testing "keyword not a number" (is (=ok false (validate-lilac :k (lilac-number nil)))))
 (testing "nil not a number" (is (=ok false (validate-lilac nil (lilac-number nil)))))
 (testing
  "number larger than 100"
  (is (=ok true (validate-lilac 101 (lilac-number {:min 100})))))
 (testing
  "99 is not larger than 100"
  (is (=ok false (validate-lilac 99 (lilac-number {:min 100}))))))

(deftest
 test-or
 (testing
  "number or string"
  (is (=ok true (validate-lilac 10 (lilac-or (lilac-number nil) (lilac-string nil))))))
 (testing
  "number or string"
  (is (=ok true (validate-lilac "10" (lilac-or (lilac-number nil) (lilac-string nil))))))
 (testing
  "keyword is not number or string"
  (is (=ok false (validate-lilac :x (lilac-or (lilac-number nil) (lilac-string nil)))))))

(deftest
 test-router-config
 (testing "valid config" (is (=ok true (validate-lilac router-data (lilac-router)))))
 (testing
  "overwriten config"
  (is
   (=ok
    false
    (validate-lilac
     (assoc-in router-data [:routes 1 :next 1 :get] "overwriten")
     (lilac-router)))))
 (testing
  "config with no file"
  (is
   (=ok
    false
    (validate-lilac
     (assoc-in router-data [:routes 1 :next 1 :get :file] nil)
     (lilac-router)))))
 (testing
  "string is not router config"
  (is (=ok false (validate-lilac "random text" (lilac-router)))))
 (testing
  "routes need to be a string"
  (is (=ok false (validate-lilac {:port 0, :routes 0} (lilac-router))))))

(deftest
 test-string
 (testing "a string" (is (=ok true (validate-lilac "x" (lilac-string nil)))))
 (testing "nil not a string" (is (=ok false (validate-lilac nil (lilac-string nil)))))
 (testing "keyword not a string" (is (=ok false (validate-lilac :x (lilac-string nil))))))

(deftest
 test-vector
 (testing
  "a vector of boolean"
  (is (=ok true (validate-lilac [true true false] (lilac-vector (lilac-boolean) nil)))))
 (testing
  "a empty vector"
  (is (=ok true (validate-lilac [] (lilac-vector (lilac-boolean) nil)))))
 (testing
  "list is not a empty vector"
  (is (=ok false (validate-lilac (list) (lilac-vector (lilac-boolean) nil)))))
 (testing
  "boolean is not a empty vector"
  (is (=ok false (validate-lilac false (lilac-vector (lilac-boolean) nil))))))
