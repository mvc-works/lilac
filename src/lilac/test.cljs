
(ns lilac.test
  (:require [cljs.test :refer [deftest is testing run-tests]]
            [lilac.core
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
              is+
              optional+]]
            [lilac.router :refer [lilac-router+ router-data]]))

(defn =ok [x obj] (= x (:ok? obj)))

(deflilac lilac-good-number+ (n) (number+ {:min n}))

(deftest
 test-and
 (testing
  "and number"
  (is (=ok true (validate-lilac 10 (and+ [(number+) (number+ {:min 0})])))))
 (testing
  "string not number"
  (is (=ok false (validate-lilac 10 (and+ [(number+) (string+)]))))))

(deftest
 test-boolean
 (testing "true is boolean" (is (=ok true (validate-lilac true (boolean+)))))
 (testing "false is boolean" (is (=ok true (validate-lilac false (boolean+)))))
 (testing "nil is no a boolean" (is (=ok false (validate-lilac nil (boolean+)))))
 (testing "string is no a boolean" (is (=ok false (validate-lilac "x" (boolean+))))))

(deftest
 test-component-args
 (testing "number 10 > 8" (is (=ok true (validate-lilac 10 (lilac-good-number+ 8)))))
 (testing "number 10 not > 18" (is (=ok false (validate-lilac 10 (lilac-good-number+ 18))))))

(deftest
 test-list
 (testing
  "a list of boolean"
  (is (=ok true (validate-lilac (list true true false) (list+ (boolean+))))))
 (testing "a empty list" (is (=ok true (validate-lilac (list) (list+ (boolean+))))))
 (testing "nil is not a list" (is (=ok false (validate-lilac nil (list+ (boolean+))))))
 (testing
  "a list of string is not list of boolean"
  (is (=ok false (validate-lilac (list "true" "false") (list+ (boolean+))))))
 (testing
  "vector is not a empty vector"
  (is (=ok false (validate-lilac [] (list+ (boolean+))))))
 (testing
  "boolean is not a empty vector"
  (is (=ok false (validate-lilac false (list+ (boolean+)))))))

(deftest
 test-map
 (testing "an empty map" (is (=ok true (validate-lilac {} (map+ [])))))
 (testing
  "an map of numbers"
  (is (=ok true (validate-lilac {1 100, 2 200} (map+ {1 (number+), 2 (number+)} nil)))))
 (testing
  "an map of numbers of not keyword/number"
  (is (=ok false (validate-lilac {:a 100, :b 200} (map+ {1 (number+), 2 (number+)} nil)))))
 (testing
  "an map of number and vector/string"
  (is
   (=ok
    true
    (validate-lilac
     {:a 100, :b ["red" "blue"]}
     (map+ {:a (number+), :b (vector+ (string+))} nil)))))
 (testing
  "add restriction to keys"
  (is
   (=ok
    false
    (validate-lilac
     {:a 100, :b ["red" "blue"]}
     (map+ {:a (number+)} {:restricted-keys #{:a}}))))))

(deftest
 test-nil
 (testing "a nil" (is (=ok true (validate-lilac nil (nil+)))))
 (testing "string not nil" (is (=ok false (validate-lilac "x" (nil+))))))

(deftest
 test-number
 (testing "a number" (is (=ok true (validate-lilac 1 (number+)))))
 (testing "keyword not a number" (is (=ok false (validate-lilac :k (number+)))))
 (testing "nil not a number" (is (=ok false (validate-lilac nil (number+)))))
 (testing
  "number larger than 100"
  (is (=ok true (validate-lilac 101 (number+ {:min 100})))))
 (testing
  "99 is not larger than 100"
  (is (=ok false (validate-lilac 99 (number+ {:min 100}))))))

(deftest
 test-optional
 (testing "optional value" (is (=ok true (validate-lilac nil (optional+ (number+))))))
 (testing
  "optional value a number"
  (is (=ok true (validate-lilac 1 (optional+ (number+))))))
 (testing
  "not not fit optional number"
  (is (=ok false (validate-lilac "1" (optional+ (number+)))))))

(deftest
 test-or
 (testing
  "number or string"
  (is (=ok true (validate-lilac 10 (or+ [(number+) (string+)])))))
 (testing
  "number or string"
  (is (=ok true (validate-lilac "10" (or+ [(number+) (string+)])))))
 (testing
  "keyword is not number or string"
  (is (=ok false (validate-lilac :x (or+ [(number+) (string+)]))))))

(deftest
 test-router-config
 (testing "valid config" (is (=ok true (validate-lilac router-data (lilac-router+)))))
 (testing
  "overwriten config"
  (is
   (=ok
    false
    (validate-lilac
     (assoc-in router-data [:routes 1 :next 1 :get] "overwriten")
     (lilac-router+)))))
 (testing
  "config with no file"
  (is
   (=ok
    false
    (validate-lilac
     (assoc-in router-data [:routes 1 :next 1 :get :file] nil)
     (lilac-router+)))))
 (testing
  "string is not router config"
  (is (=ok false (validate-lilac "random text" (lilac-router+)))))
 (testing
  "routes need to be a string"
  (is (=ok false (validate-lilac {:port 0, :routes 0} (lilac-router+))))))

(deftest
 test-string
 (testing "a string" (is (=ok true (validate-lilac "x" (string+)))))
 (testing "nil not a string" (is (=ok false (validate-lilac nil (string+)))))
 (testing "keyword not a string" (is (=ok false (validate-lilac :x (string+))))))

(deftest
 test-vector
 (testing
  "a vector of boolean"
  (is (=ok true (validate-lilac [true true false] (vector+ (boolean+))))))
 (testing "a empty vector" (is (=ok true (validate-lilac [] (vector+ (boolean+))))))
 (testing
  "list is not a empty vector"
  (is (=ok false (validate-lilac (list) (vector+ (boolean+))))))
 (testing
  "boolean is not a empty vector"
  (is (=ok false (validate-lilac false (vector+ (boolean+)))))))
