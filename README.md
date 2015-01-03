# hammock

a cljs library that helps you transform one tree into another and to remember related branches.

[![Clojars Project](http://clojars.org/hammock/latest-version.svg)](http://clojars.org/hammock)

![illustration](hammock.png)

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
SRC-KEYS       DST-KEYS
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
                 [:sum]
[:myBar]  ---->  [:my-bar :value]
                 [:sum]
```

And a destination->source mapping would look like:

```
DST-KEYS                 SRC-KEYS
------------------------------------------
[:my-foo :value]  ---->  [:myFoo]
[:my-bar :value]  ---->  [:myBar]
[:sum]            ---->  [:myFoo]
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
;; => {[:myFoo] #{[:my-foo :value]}
;;     [:myBar] #{[:my-bar :value]}}

(-> dst meta :anchors :inverse)
;; => {[:my-foo :value] #{[:myFoo]}
;;     [:my-bar :value] #{[:myBar]}}
```

There is a command for manually setting a destination value and its dependent source keys:

```clj
(let [foo (:myFoo h)
      bar (:myBar h)
      sum (+ foo bar)]
  (hm/man! h :sum sum [:myFoo :myBar]))
```

And the new result will reflect the addition:

```clj
(def dst (hm/result h))
;; => {:my-foo {:value 1}
;;     :my-bar {:value 2}
;;     :sum    {:value 3}}

(-> dst meta :anchors :forward)
;; => {[:myFoo] #{[:my-foo :value] [:sum]}
;;     [:myBar] #{[:my-bar :value] [:sum]}}

(-> dst meta :anchors :inverse)
;; => {[:my-foo :value] #{[:myFoo]}
;;     [:my-bar :value] #{[:myBar]}
;;     [:sum]           #{[:myFoo] [:myBar]}}
```

## Details

Hammocks are a bit like [Om cursors], except they are anchored to two separate
trees: a read-only "source" tree and write-only "destination" tree. These
anchor points on the hammock move along their respective trees as data is
transformed from source to destination.  A log of the anchor positions is kept
for each transformation in order to remember the relationship between source
and destination branches.

You create a hammock by giving the constructor function an existing tree of
data to be transformed.

```
;; Create a hammock starting with some tree to be transformed.
(def h (hm/create src))
```

With this hammock, you can start building a new tree using hammock operations
acting on the original tree.  All operations are tracked so you can relate the
nodes in both trees.

```clj
;; NOTE: a "key" can be a keyword or a vector of keywords.
;; it can also be [] to denote current path (TODO: must find allowed cases)

;; Set dest value to src value. Applying function to the src value if given.
(hm/copy! h :dest-key :src-key data-fn?)

;; Start a nested transaction by moving the hammock to the given relative keys,
;; and passing it to the given function.
(hm/nest! h :dest-key :src-key hammock-fn)

;; Map the src seq to the dest seq with the given hammock function.  (If you wish
;; to use a function that doesn't take a hammock, use copy! with a mapv function.
;; Nested operations will not be remembered though)
(hm/map! h :dest-key :src-key hammock-fn)

;; Manually set dest to the given value. Optionally include a set of src-keys used
;; to compute the value. This is intended to be used when a dest value depends on
;; multiple src values or vice versa.
(hm/man! h :dest-key value src-keys?)

;; Lookup operations are performed on the read-only "source" tree.  It is intended
;; to assist in computing intermediate values that may be shared across multiple
;; `man!` commands.
(:src-key h)
```

After you are done building the new tree, you can read it out of the hammock object.
You can also read out the "anchor" maps that map any given branch to the related
branches in the other tree.

```clj
;; Retrieve the newly transformed tree.
(hm/result h)

;; The anchors representing the relationship between the two trees is attached
;; as metadata to the result.
(let [result (hm/result h)
      anchors (-> result meta :anchors)]
  (:forward anchors)  ;; => maps old-path to related new-paths
  (:inverse anchors)) ;; => maps new-path to related old-paths
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
