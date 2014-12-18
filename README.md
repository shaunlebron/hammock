# Hammock

(experimental work in progress)

In UIs, it is common to transform JSON data received from a backend REST
service into data better suited for representation on screen.  After this
transformation is made, it is useful to remember which original fields are
associated with the new ones.

This is an experiment to capture that relationship during the actual process of
transformation, by performing the transformation with objects we are calling
"hammocks."

Hammocks are a bit like [Om cursors], except they are anchored to two separate
trees: a read-only "source" tree and write-only "destination" tree. The
following functions are created to perform and remember simple transformations:

## Creating

You create a hammock by giving the constructor function an existing tree of
data to be transformed.

```clj
;; Create a hammock starting with some tree to be transformed.
(def h (hm/create some-tree))
```

## Tree Builder Operations

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

## Reading Results

After you are done building the new tree, you can read it out of the hammock object.
You can also read out the "anchor" maps that map any given branch to the related
branches in the other tree.

```clj
;; Retrieve the newly transformed tree.
(hm/new-tree h)

;; Retrieve the anchors representing the relationship between the two trees.
(let [anchors (hm/anchors h)]
  (:forward anchors)  ;; => maps old-path to related new-paths
  (:inverse anchors)) ;; => maps new-path to related old-paths

```

[Om cursors]: https://github.com/swannodette/om/wiki/Cursors
