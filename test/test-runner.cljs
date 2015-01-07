(ns test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [hammock.tests]))


(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
        (run-tests
          'hammock.tests))
    0
    1))
