(ns hammock.tests
  (:require
    [hammock.core :as hm]
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-basic
  (testing "Testing lookup on destination anchor."
    (let [src {:foo "bar" :bar ["hi" "there"]}
          h (hm/create src)]
      (is (= "bar" (:foo h)))
      (is (= "there" (get-in h [:bar 1])))))
  (testing "Testing copy!"
    (let [src {:fooBar "hi" :booFar "bye"}
          dst (atom {})
          h (hm/create src dst)]
      (hm/copy! h :foo-bar :fooBar)
      (hm/copy! h :boo-far :booFar)
      (is (= "hi" (:foo-bar @dst)))
      (is (= "bye" (:boo-far @dst)))))
  (testing "Testing nest!"
    (let [src {:fooBar "hi" :booFar "bye"}
          dst (atom {})
          h (hm/create src dst)]
      (hm/nest! h :foo [] (fn [h] (hm/copy! h :bar :fooBar)))
      (hm/nest! h :boo [] (fn [h] (hm/copy! h :far :booFar)))
      (is (= "hi" (-> @dst :foo :bar)))
      (is (= "bye" (-> @dst :boo :far)))))
  (testing "Testing man!"
    (let [src {:fooBar "hi" :booFar "bye"}
          dst (atom {})
          h (hm/create src dst)
          hibye (str (:fooBar h) (:booFar h))
          byehi (str (:booFar h) (:fooBar h))]
      (hm/man! h [:foo :bar] hibye [:fooBar :booFar])
      (hm/man! h [:boo :far] byehi [:booFar :fooBar])
      (is (= "hibye" (-> @dst :foo :bar)))
      (is (= "byehi" (-> @dst :boo :far)))))
  )

(enable-console-print!)

(run-tests)

