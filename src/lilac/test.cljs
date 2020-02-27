
(ns lilac.test
  (:require [cljs.test :refer [deftest is testing run-tests]]
            [lilac.core
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
              tuple+
              list+
              record+
              enum+
              map+
              not+
              any+
              and+
              set+
              nil+
              or+
              is+
              register-custom-rule!]]
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
 test-any
 (testing "a nil" (is (=ok true (validate-lilac nil (any+)))))
 (testing "any in string" (is (=ok true (validate-lilac "x" (any+)))))
 (testing "something" (is (=ok true (validate-lilac "x" (any+ {:some? true})))))
 (testing "need something" (is (=ok false (validate-lilac nil (any+ {:some? true}))))))

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
 test-custom
 (let [method-1 (fn [x]
                  (if (and (> x 10) (< x 20))
                    {:ok? true}
                    {:ok? false, :message (str "expects number between 10 amd 20, got " x)}))]
   (testing
    "validating number with custom function"
    (is (=ok true (validate-lilac 11 (custom+ method-1)))))
   (testing
    "validating number with custom function"
    (is (=ok false (validate-lilac 21 (custom+ method-1))))))
 (let [validate-method-2 (fn [data rule coord]
                           (if (and (> data 10) (< data 20))
                             {:ok? true}
                             {:ok? false,
                              :data data,
                              :rule rule,
                              :coord coord,
                              :message (str "expects number between 10 amd 20, got " data)}))
       method-2+ (fn [] {:lilac-type :method-2})]
   (register-custom-rule! :method-2 validate-method-2)
   (testing
    "validating number with custom function"
    (is (=ok true (validate-lilac 11 (method-2+)))))
   (testing
    "validating number with custom function"
    (is (=ok false (validate-lilac 21 (method-2+)))))))

(deftest
 test-enum
 (testing "1 in enum" (is (=ok true (validate-lilac 1 (enum+ #{1 2 3 "4"})))))
 (testing "string 4 in enum" (is (=ok true (validate-lilac "4" (enum+ #{1 2 3 "4"})))))
 (testing "4 not in enum" (is (=ok false (validate-lilac 4 (enum+ #{1 2 3 "4"})))))
 (testing
  "100 not in enum with vector"
  (is (=ok false (validate-lilac 100 (enum+ [1 2 3]))))))

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
  (is (=ok false (validate-lilac false (list+ (boolean+))))))
 (testing
  "allow vector for list"
  (is (=ok true (validate-lilac [1] (list+ (number+) {:allow-vector? true}))))))

(deftest
 test-map
 (testing
  "a map of strings"
  (is (=ok true (validate-lilac {"a" "a", "b" "b"} (map+ (string+) (string+))))))
 (testing
  "a map of strings has no keyword"
  (is (=ok false (validate-lilac {:a "a", "b" "b"} (map+ (string+) (string+))))))
 (testing
  "a map of keyword/number"
  (is (=ok true (validate-lilac {:a 1, :b 2} (map+ (keyword+) (number+))))))
 (testing
  "a map of keyword/number not number/keyword"
  (is (=ok false (validate-lilac {:a 1, 2 :b} (map+ (keyword+) (number+))))))
 (testing
  "a map of keyword/number or keyword/string"
  (is
   (=ok
    true
    (validate-lilac {:a 1, :b "two"} (map+ (keyword+) (or+ [(number+) (string+)])))))))

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
 test-optional-record
 (testing
  "record with optional"
  (is
   (=ok
    false
    (validate-lilac
     {1 100}
     (record+ {1 (number+), 2 (number+)} {:all-optional? false, :check-keys? true})))))
 (testing
  "record not with optional"
  (is
   (=ok
    true
    (validate-lilac
     {1 100}
     (record+ {1 (number+), 2 (number+)} {:all-optional? true, :check-keys? true}))))))

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
 test-record
 (testing "an empty record" (is (=ok true (validate-lilac {} (record+ [])))))
 (testing
  "an record of numbers"
  (is (=ok true (validate-lilac {1 100, 2 200} (record+ {1 (number+), 2 (number+)} nil)))))
 (testing
  "an record of numbers of not keyword/number"
  (is
   (=ok false (validate-lilac {:a 100, :b 200} (record+ {1 (number+), 2 (number+)} nil)))))
 (testing
  "an record of number and vector/string"
  (is
   (=ok
    true
    (validate-lilac
     {:a 100, :b ["red" "blue"]}
     (record+ {:a (number+), :b (vector+ (string+))} nil)))))
 (testing
  "exact two keys"
  (is
   (=ok
    false
    (validate-lilac
     {:a 100, :b ["red" "blue"]}
     (record+ {:a (number+)} {:exact-keys? true})))))
 (testing
  "exact two keys"
  (is
   (=ok
    false
    (validate-lilac {:a 100} (record+ {:a (number+), :b (number+)} {:exact-keys? true})))))
 (testing
  "check two keys"
  (is
   (=ok
    false
    (validate-lilac
     {:a 100, :b ["red" "blue"]}
     (record+ {:a (number+)} {:check-keys? true})))))
 (testing
  "check two keys"
  (is
   (=ok
    false
    (validate-lilac {:a 100} (record+ {:a (number+), :b (number+)} {:check-keys? true})))))
 (testing
  "confirm keys"
  (is
   (=ok
    true
    (validate-lilac {:a 1, :b 1} (record+ {:a (number+), :b (number+)} {:exact-keys? true}))))))

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
 (testing "keyword not a string" (is (=ok false (validate-lilac :x (string+)))))
 (testing "blank string" (is (=ok true (validate-lilac "" (string+ {:nonblank? false})))))
 (testing "blank string" (is (=ok false (validate-lilac "" (string+ {:nonblank? true})))))
 (testing "blank string" (is (=ok true (validate-lilac "x" (string+ {:nonblank? true}))))))

(deftest
 test-tuple
 (testing "an empty tuple" (is (=ok true (validate-lilac [] (tuple+ [])))))
 (testing
  "check an empty tuple in list"
  (is (=ok false (validate-lilac (list) (tuple+ [])))))
 (testing
  "an empty tuple in list"
  (is (=ok true (validate-lilac (list) (tuple+ [] {:in-list? true})))))
 (testing
  "tuple of number string boolean"
  (is (=ok true (validate-lilac [1 "1" true] (tuple+ [(number+) (string+) (boolean+)])))))
 (testing
  "tuple not vector"
  (is
   (=ok false (validate-lilac (list 1 "1" true) (tuple+ [(number+) (string+) (boolean+)])))))
 (testing
  "tuple not right type"
  (is (=ok false (validate-lilac [1 "1" true] (tuple+ [(number+) (number+) (boolean+)])))))
 (testing
  "tuple not right type"
  (is (=ok false (validate-lilac [1 "1"] (tuple+ [(number+)] {:check-size? true})))))
 (testing
  "tuple not right type"
  (is (=ok true (validate-lilac [1 "1"] (tuple+ [(number+)] {:check-size? false}))))))

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
  (is (=ok false (validate-lilac false (vector+ (boolean+))))))
 (testing
  "allow list in vector"
  (is (=ok true (validate-lilac (list 1) (vector+ (number+) {:allow-list? true}))))))
