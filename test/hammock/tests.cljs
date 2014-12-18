(ns hammock.tests
  (:require
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-basic
  (testing "Testing basic arithmetic"
    (is (= 2 (+ 1 1)))
    (is (= 6 (* 3 2)))))

(enable-console-print!)

(run-tests)

