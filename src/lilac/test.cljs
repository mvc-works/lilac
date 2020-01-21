
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
