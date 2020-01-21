
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
              lilac-custom]]))

(deflilac
 lilac-router
 ()
 (lilac-map {:port (lilac-number nil), :routes (lilac-vector (lilac-map nil nil) nil)} nil))

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
  (is
   (=
    true
    (:ok? (validate-lilac 10 (lilac-and (lilac-number nil) (lilac-number {:min 0})))))))
 (testing
  "string not number"
  (is
   (= false (:ok? (validate-lilac 10 (lilac-and (lilac-number nil) (lilac-string nil))))))))

(deftest
 test-boolean
 (testing "true is boolean" (is (= true (:ok? (validate-lilac true (lilac-boolean))))))
 (testing "false is boolean" (is (= true (:ok? (validate-lilac false (lilac-boolean))))))
 (testing "nil is no a boolean" (is (= false (:ok? (validate-lilac nil (lilac-boolean))))))
 (testing
  "string is no a boolean"
  (is (= false (:ok? (validate-lilac "x" (lilac-boolean)))))))

(deftest
 test-list
 (testing
  "a list of boolean"
  (is
   (= true (:ok? (validate-lilac (list true true false) (lilac-list (lilac-boolean) nil))))))
 (testing
  "a empty list"
  (is (= true (:ok? (validate-lilac (list) (lilac-list (lilac-boolean) nil))))))
 (testing
  "nil is not a list"
  (is (= false (:ok? (validate-lilac nil (lilac-list (lilac-boolean) nil))))))
 (testing
  "a list of string is not list of boolean"
  (is
   (= false (:ok? (validate-lilac (list "true" "false") (lilac-list (lilac-boolean) nil))))))
 (testing
  "vector is not a empty vector"
  (is (= false (:ok? (validate-lilac [] (lilac-list (lilac-boolean) nil))))))
 (testing
  "boolean is not a empty vector"
  (is (= false (:ok? (validate-lilac false (lilac-list (lilac-boolean) nil)))))))

(deftest
 test-map
 (testing "an empty map" (is (= true (:ok? (validate-lilac {} (lilac-map [] nil))))))
 (testing
  "an map of numbers"
  (is
   (=
    true
    (:ok?
     (validate-lilac
      {1 100, 2 200}
      (lilac-map {1 (lilac-number nil), 2 (lilac-number nil)} nil))))))
 (testing
  "an map of numbers of not keyword/number"
  (is
   (=
    false
    (:ok?
     (validate-lilac
      {:a 100, :b 200}
      (lilac-map {1 (lilac-number nil), 2 (lilac-number nil)} nil))))))
 (testing
  "an map of number and vector/string"
  (is
   (=
    true
    (:ok?
     (validate-lilac
      {:a 100, :b ["red" "blue"]}
      (lilac-map {:a (lilac-number nil), :b (lilac-vector (lilac-string nil) nil)} nil)))))))

(deftest
 test-nil
 (testing "a nil" (is (= true (:ok? (validate-lilac nil (lilac-nil))))))
 (testing "string not nil" (is (= false (:ok? (validate-lilac "x" (lilac-nil)))))))

(deftest
 test-number
 (testing "a number" (is (= true (:ok? (validate-lilac 1 (lilac-number nil))))))
 (testing
  "keyword not a number"
  (is (= false (:ok? (validate-lilac :k (lilac-number nil))))))
 (testing "nil not a number" (is (= false (:ok? (validate-lilac nil (lilac-number nil))))))
 (testing
  "number larger than 100"
  (is (= true (:ok? (validate-lilac 101 (lilac-number {:min 100}))))))
 (testing
  "99 is not larger than 100"
  (is (= false (:ok? (validate-lilac 99 (lilac-number {:min 100})))))))

(deftest
 test-or
 (testing
  "number or string"
  (is (= true (:ok? (validate-lilac 10 (lilac-or (lilac-number nil) (lilac-string nil)))))))
 (testing
  "number or string"
  (is
   (= true (:ok? (validate-lilac "10" (lilac-or (lilac-number nil) (lilac-string nil)))))))
 (testing
  "keyword is not number or string"
  (is (= false (:ok? (validate-lilac :x (lilac-or (lilac-number nil) (lilac-string nil))))))))

(deftest
 test-router-config
 (testing (is (= true (:ok? (validate-lilac router-data lilac-router)))))
 (testing (is (= false (:ok? (validate-lilac "random text" lilac-router)))))
 (testing (is (= false (:ok? (validate-lilac {:port 0, :routes 0} lilac-router))))))

(deftest
 test-string
 (testing "a string" (is (= true (:ok? (validate-lilac "x" (lilac-string nil))))))
 (testing "nil not a string" (is (= false (:ok? (validate-lilac nil (lilac-string nil))))))
 (testing
  "keyword not a string"
  (is (= false (:ok? (validate-lilac :x (lilac-string nil)))))))

(deftest
 test-vector
 (testing
  "a vector of boolean"
  (is (= true (:ok? (validate-lilac [true true false] (lilac-vector (lilac-boolean) nil))))))
 (testing
  "a empty vector"
  (is (= true (:ok? (validate-lilac [] (lilac-vector (lilac-boolean) nil))))))
 (testing
  "list is not a empty vector"
  (is (= false (:ok? (validate-lilac (list) (lilac-vector (lilac-boolean) nil))))))
 (testing
  "boolean is not a empty vector"
  (is (= false (:ok? (validate-lilac false (lilac-vector (lilac-boolean) nil)))))))
