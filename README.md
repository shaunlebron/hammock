# Hammock

(experimental work in progress)

In UIs, it is common to transform JSON data received from a backend REST
service into data better suited for representation on screen.  After this
transformation is made, it is useful to remember which original fields are
associated with the new ones.

This is an experiment to capture that relationship during the actual process of
transformation, by performing the transformation with objects we are calling
"hammocks."

Hammocks are a bit like Om cursors, except they are anchored to two separate
trees: a read-only "source" tree and read-write "destination" tree. The
following functions are created to perform and remember simple transformations:

```clj
;; Set dest value to src value. Applying function to the src value if given.
(ham/set! h :dest-key :src-key [data-fn])

;; Start a nested transaction by pushing the hammock further down the trees.
;; Pass the new nested hammock to the given function for further transforming.
(ham/nest! h :dest-key :src-key hammock-fn)

;; Set dest seq to src seq by mapping with given hammock function, allowing for
;; further nesting.  (If you wish to use a function that doesn't take a hammock,
;; use set! with a mapv function. Nested operations will not be remembered though)
(ham/nest-map! h :dest-key :src-key hammock-fn)

;; NOTE: a "key" can be a keyword or a vector of keywords.
;; it can also be [] to denote current path (TODO: must find allowed cases)
```

All operations are tracked by an "anchor" map, which maps src paths to dest
paths, representing the operations that took place during the transformation.

## Examples

```clj
(def old-tree (atom {:foo-bar "hello"
                     :foo-baz "bye"
                     :hum {:digMin 10
                           :digMax 20
                           :dupMin 12
                           :dupMax 30}))
(def new-tree (atom {}))
(def anchors (atom {}))

(require '[hammock.core :as ham])

(def h (ham/create old-tree new-tree anchors))

;; ============================================

(ham/set! h [:foo :bar] [:foo-bar])

@new-tree
;; => {:foo {:bar "hello"}}

@anchors
;; => {[:foo-bar] [:foo :bar]}

;; ============================================

(ham/set! h [:foo :baz] [:foo-baz])

@new-tree
;; => {:foo {:bar "hello"
             :baz "bye"}}

@anchors
;; => {[:foo-bar] [:foo :bar]
       [:foo-baz] [:foo :baz]}

;; ============================================

;; nest example
;; nest-map example
```

