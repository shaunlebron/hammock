(ns hammock.tests
  (:require
    [hammock.core :as hm]
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-basic
  (testing "Testing lookup"
    (let [src {:foo "bar" :bar ["hi" "there"]}
          h (hm/create src)]
      (is (= "bar" (:foo h)))
      (is (= "there" (get-in h [:bar 1])))))
  (testing "Testing nested lookup."
    (let [src {:foo {:bar "hi"}}
          dst (atom {})
          anchors (atom {})
          h (hm/create src [:foo] dst [] anchors)
          h2 (hm/create src :foo dst [] anchors)]
      (is (= "hi" (:bar h)))
      (is (= "hi" (:bar h2)))))
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
  (testing "Testing map!"
    (let [src {:foo [{:a 1 :b 2}
                     {:a 3 :b 4}
                     {:a 5 :b 6}]}
          dst (atom {})
          expected-dst {:foo {:things [{:a 1 :c 3}
                                       {:a 3 :c 7}
                                       {:a 5 :c 11}]}}
          h (hm/create src dst)
          mapping (fn [h]
                    (hm/copy! h :a :a)
                    (hm/man! h :c (+ (:a h) (:b h)) [:a :b]))]
      (hm/map! h [:foo :things] :foo mapping)
      (is (= @dst expected-dst))))
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

