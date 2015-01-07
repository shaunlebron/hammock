# hammock

a cljs library that helps you transform one tree into another and to remember related branches.

[![Clojars Project](http://clojars.org/hammock/latest-version.svg)](http://clojars.org/hammock)

![illustration](hammock.png)

Hammocks are a bit like [Om cursors], except they are anchored to two separate
trees: a read-only "source" tree and write-only "destination" tree. These
anchor points on the hammock move along their respective trees as data is
transformed from source to destination.  A log of the anchor positions is kept
for each transformation in order to remember the relationship between source
and destination branches.

(Motivated by the desire to track complex frontend <--> backend data transformations.)

## Usage

Add to your dependencies vector in project.clj:

```
[hammock "0.2.0"]
```

```clj
(ns example
 (:require [hammock.core :as hm]))
```

## Transforms and Mappings

Suppose you have a data in some source format:

```clj
{:myFoo 1
 :myBar 2}
```

And you want to transform it into some destination format:

```clj
{:my-foo {:value 1}
 :my-bar {:value 2}}
```

Also, you want to remember the mapping between the two formats:

```
SRC-KEYS         DST-KEYS
----------------------------------
[:myFoo]  <--->  [:my-foo :value]
[:myBar]  <--->  [:my-bar :value]
```

Well sometimes a destination value can depend on multiple source values:

```clj
{:my-foo {:value 1}
 :my-bar {:value 2}
 :sum    {:value 3}} ;; <--- myFoo + myBar
```

So a source->destination mapping would now look like:

```
SRC-KEYS         DST-KEYS
----------------------------------------
[:myFoo]  ---->  [:my-foo :value]
                 [:sum :value]
[:myBar]  ---->  [:my-bar :value]
                 [:sum :value]
```

And a destination->source mapping would look like:

```
DST-KEYS                 SRC-KEYS
------------------------------------------
[:my-foo :value]  ---->  [:myFoo]
[:my-bar :value]  ---->  [:myBar]
[:sum :value]     ---->  [:myFoo]
                         [:myBar]
```

## Using hammock

You can perform such data transformations using __hammock__:

```clj
(def src {:myFoo 1 :myBar 2})

(def h (hm/create src))
(hm/copy! h [:my-foo :value] :myFoo)
(hm/copy! h [:my-bar :value] :myBar)
```

And it will produce the desired destination format:

```clj
(def dst (hm/result h))
;; => {:my-foo {:value 1}
;;     :my-bar {:value 2}}
```

And the `:anchors` metadata will remember the forward/inverse mappings of the
keys between the formats.

```clj
(-> dst meta :anchors :forward)
;;     SRC-KEYS     DST-KEYS
;; => {[:myFoo]   #{[:my-foo :value]}
;;     [:myBar]   #{[:my-bar :value]}}

(-> dst meta :anchors :inverse)
;;     DST-KEYS            SRC-KEYS
;; => {[:my-foo :value]  #{[:myFoo]}
;;     [:my-bar :value]  #{[:myBar]}}
```

### Manual writing

There is a command for manually setting a destination value, which is useful
for a computing destination value from multiple source values.

```clj
(def sum (+ (:myFoo src) (:myBar src)))
(hm/man! h [:sum :value] sum)
```

You can include optional dependent source keys as the last argument so we can
trace those keys to our computed value:

```clj
(hm/man! h [:sum :value] sum [:myFoo :myBar])
```

And the new result will reflect the addition:

```clj
(def dst (hm/result h))
;; => {:my-foo {:value 1}
;;     :my-bar {:value 2}
;;     :sum    {:value 3}}

(-> dst meta :anchors :forward)
;;     SRC-KEYS     DST-KEYS
;; => {[:myFoo]   #{[:my-foo :value]
;;                  [:sum :value]}
;;     [:myBar]   #{[:my-bar :value]
;;                  [:sum :value]}}

(-> dst meta :anchors :inverse)
;;     DST-KEYS            SRC-KEYS
;; => {[:my-foo :value]  #{[:myFoo]}
;;     [:my-bar :value]  #{[:myBar]}
;;     [:sum :value]     #{[:myFoo]
;;                         [:myBar]}}
```

### Composability

We can create composable transformations using functions that take a
hammock object `h`:

```clj
(defn unpack-thing [h]
  (hm/copy! h [:my-foo :value] :myFoo)
  (hm/copy! h [:my-bar :value] :myBar))
```

We can then use this function to perform sub-transformations.  We do this by
passing the function to `hm/nest!`, causing it to receive a relative hammock
whose anchors are moved to the given keys.

```clj
(def src {:a {:foo 1 :bar 2}
          :b {:foo 3 :bar 4}})

(def h (hm/create src))

(hm/nest! h :my-a :a unpack-thing)
(hm/nest! h :my-b :b unpack-thing)

(hm/result h)
;; => {:my-a {:my-foo {:value 1}
;;            :my-bar {:value 2}}
;;     :my-b {:my-foo {:value 3}
;;            :my-bar {:value 4}}}
```

And we can update `unpack-thing` to manually create a sum value:

```clj
(defn unpack-thing [h]
  (hm/copy! h [:my-foo :value] :myFoo)
  (hm/copy! h [:my-bar :value] :myBar)

  (let [sum (+ (:myFoo h) (:myBar h))
        keys-used [:myFoo :myBar]]
    (hm/man! h [:sum :value] sum keys-used)))

(hm/nest! h :my-a :a unpack-thing)
(hm/nest! h :my-b :b unpack-thing)

(hm/result h)
;; => {:my-a {:my-foo {:value 1}
;;            :my-bar {:value 2}
;;            :sum    {:value 3}}  ;; <-- added sum
;;     :my-b {:my-foo {:value 3}
;;            :my-bar {:value 4}
;;            :sum    {:value 7}}} ;; <-- added sum
```

__IMPORTANT__: `(:myFoo h)` is a helpful shorthand for reading source values at the
current hammock position.

### Sequences

There is support for simple 1-to-1 vector transformations using `hm/map!`.

```clj
(def src {:vals [{:foo 1 :bar 2}
                 {:foo 3 :bar 4}]})

(def h (hm/create src))

(hm/map! h :my-vals :vals unpack-thing)

(def dst (hm/result h))
;; => {:my-vals [{:my-foo {:value 1}
;;                :my-bar {:value 2}
;;                :sum    {:value 3}}
;;               {:my-foo {:value 3}
;;                :my-bar {:value 4}
;;                :sum    {:value 7}}]}
```

You can see the resulting anchors below:

```clj
(-> dst meta :anchors :forward)
;;     SRC-KEYS               DST-KEYS
;; => {[:vals 0 :myFoo]     #{[:my-vals 0 :my-foo :value]
;;                            [:my-vals 0 :sum    :value]}
;;     [:vals 0 :myBar]     #{[:my-vals 0 :my-bar :value]
;;                            [:my-vals 0 :sum    :value]}
;;     [:vals 1 :myFoo]     #{[:my-vals 1 :my-foo :value]
;;                            [:my-vals 1 :sum    :value]}
;;     [:vals 1 :myBar]     #{[:my-vals 1 :my-bar :value]
;;                            [:my-vals 1 :sum    :value]}}

(-> dst meta :anchors :inverse)
;;     DST-KEYS                        SRC-KEYS
;; => {[:my-vals 0 :my-foo :value]   #{[:vals 0 :myFoo]}
;;     [:my-vals 0 :my-bar :value]   #{[:vals 0 :myBar]}
;;     [:my-vals 0 :sum    :value]   #{[:vals 0 :myFoo]
;;                                     [:vals 0 :myBar]}
;;     [:my-vals 1 :my-foo :value]   #{[:vals 1 :myFoo]}
;;     [:my-vals 1 :my-bar :value]   #{[:vals 1 :myBar]}
;;     [:my-vals 1 :sum    :value]   #{[:vals 1 :myFoo]
;;                                     [:vals 1 :myBar]}}
```

## Running tests

1. Build the tests.

    ```
    lein cljsbuild auto
    ```

1. Open "script/run_tests.html" in a browser.
1. See test results in javascript console.

## License

Copyright © 2014 Shaun Williams

Distributed under the Eclipse Public License either version 1.0 or any
later version.

[Om cursors]: https://github.com/swannodette/om/wiki/Cursors
