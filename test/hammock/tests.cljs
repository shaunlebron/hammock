(ns hammock.tests
  (:require
    [hammock.core :as hm]
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-basic
  (testing "Testing lookup on destination anchor."
    (let [src {:foo "bar" :bar ["hi" "there"]}
          h (hm/create src)]
      (is (= "bar" (:foo h)))
      (is (= "there" (get-in h [:bar 1]))))))

(enable-console-print!)

(run-tests)

