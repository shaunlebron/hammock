(ns hammock.tests
  (:require
    [hammock.core :as hm]
    [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-basic
  (testing "Testing read-out operations."
    (let [src {:foo "bar" :bar ["hi" "there"]}
          dst (atom {:hello "world"})
          h (hm/create src dst)]
      (is (= src (hm/src-tree h)))
      (is (= @dst (hm/dst-tree h)))
      (is (= {} (hm/anchors h)))))
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
          anchors (atom {})
          h (hm/create src dst anchors)
          expected-anchors {:forward {[:fooBar] #{[:foo-bar]}
                                      [:booFar] #{[:boo-far]}}
                            :inverse {[:foo-bar] #{[:fooBar]}
                                      [:boo-far] #{[:booFar]}}}]
      (hm/copy! h :foo-bar :fooBar)
      (hm/copy! h :boo-far :booFar)
      (is (= "hi" (:foo-bar @dst)))
      (is (= "bye" (:boo-far @dst)))
      (is (= @anchors expected-anchors))
      (let [result (hm/result h)
            expected-result {:foo-bar "hi" :boo-far "bye"}
            result-anchors (-> result meta :anchors)]
        (is (= result expected-result))
        (is (= result-anchors expected-anchors)))))
  (testing "Testing nest!"
    (let [src {:fooBar "hi" :booFar "bye"}
          dst (atom {})
          anchors (atom {})
          h (hm/create src dst anchors)
          expected-anchors {:forward {[:fooBar] #{[:foo :bar]}
                                      [:booFar] #{[:boo :far]}}
                            :inverse {[:foo :bar] #{[:fooBar]}
                                      [:boo :far] #{[:booFar]}}}]
      (hm/nest! h :foo [] (fn [h] (hm/copy! h :bar :fooBar)))
      (hm/nest! h :boo [] (fn [h] (hm/copy! h :far :booFar)))
      (is (= "hi" (-> @dst :foo :bar)))
      (is (= "bye" (-> @dst :boo :far)))
      (is (= @anchors expected-anchors))))
  (testing "Testing map!"
    (let [src {:foo [{:a 1 :b 2}
                     {:a 3 :b 4}
                     {:a 5 :b 6}]}
          dst (atom {})
          anchors (atom {})
          expected-dst {:foo {:things [{:a 1 :c 3}
                                       {:a 3 :c 7}
                                       {:a 5 :c 11}]}}
          h (hm/create src dst anchors)
          mapping (fn [h]
                    (hm/copy! h :a :a)
                    (hm/man! h :c (+ (:a h) (:b h)) [:a :b]))
          expected-anchors {:forward {[:foo 0 :a] #{[:foo :things 0 :a] [:foo :things 0 :c]}
                                      [:foo 0 :b] #{[:foo :things 0 :c]}
                                      [:foo 1 :a] #{[:foo :things 1 :a] [:foo :things 1 :c]}
                                      [:foo 1 :b] #{[:foo :things 1 :c]}
                                      [:foo 2 :a] #{[:foo :things 2 :a] [:foo :things 2 :c]}
                                      [:foo 2 :b] #{[:foo :things 2 :c]}}
                            :inverse {[:foo :things 0 :a] #{[:foo 0 :a]}
                                      [:foo :things 0 :c] #{[:foo 0 :a] [:foo 0 :b]}
                                      [:foo :things 1 :a] #{[:foo 1 :a]}
                                      [:foo :things 1 :c] #{[:foo 1 :a] [:foo 1 :b]}
                                      [:foo :things 2 :a] #{[:foo 2 :a]}
                                      [:foo :things 2 :c] #{[:foo 2 :a] [:foo 2 :b]}}}]
      (hm/map! h [:foo :things] :foo mapping)
      (is (= @dst expected-dst))
      (is (= @anchors expected-anchors))))
  (testing "Testing man!"
    (let [src {:fooBar "hi" :booFar "bye"}
          dst (atom {})
          anchors (atom {})
          h (hm/create src dst anchors)
          hibye (str (:fooBar h) (:booFar h))
          byehi (str (:booFar h) (:fooBar h))
          expected-anchors {:forward {[:fooBar] #{[:foo :bar] [:boo :far]}
                                      [:booFar] #{[:foo :bar] [:boo :far]}}
                            :inverse {[:foo :bar] #{[:fooBar] [:booFar]}
                                      [:boo :far] #{[:booFar] [:fooBar]}}}]
      (hm/man! h [:foo :bar] hibye [:fooBar :booFar])
      (hm/man! h [:boo :far] byehi [:booFar :fooBar])
      (is (= "hibye" (-> @dst :foo :bar)))
      (is (= "byehi" (-> @dst :boo :far)))
      (is (= @anchors expected-anchors)))))

(enable-console-print!)

(run-tests)

