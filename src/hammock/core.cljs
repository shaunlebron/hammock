(ns hammock.core)

(defprotocol IHammock
  (-dst-tree [this])
  (-src-tree [this])
  (-anchors [this])
  (-copy! [this dst-key src-key d-fn])
  (-nest! [this dst-key src-key h-fn])
  (-map!  [this dst-key src-key h-fn])
  (-man!  [this dst-ks value src-keys]))

(defn- norm-path
  [k]
  (if (sequential? k) (vec k) [k]))

(defn- join-path
  [path k]
  (vec (concat path (norm-path k))))

(defn- remember-anchor!
  [key-path val-path anchors dir]
  (if (get-in @anchors [dir key-path])
    (swap! anchors update-in [dir key-path] conj val-path)
    (swap! anchors assoc-in [dir key-path] (hash-set val-path))))

(defn- remember-anchors!
  [src-path dst-path anchors]
  (remember-anchor! src-path dst-path anchors :forward)
  (remember-anchor! dst-path src-path anchors :inverse))

(deftype Hammock [src src-path dst dst-path anchors]
  IHammock
  (-src-tree [this] src)
  (-dst-tree [this] @dst)
  (-anchors [this] @anchors)

  (-copy! [this dst-key src-key d-fn]
    (let [src-path (join-path src-path src-key)
          dst-path (join-path dst-path dst-key)
          src-val (d-fn (get-in src src-path))]
      (swap! dst assoc-in dst-path src-val)
      (remember-anchors! src-path dst-path anchors)))

  (-nest! [this dst-key src-key h-fn]
    (let [src-path (join-path src-path src-key)
          dst-path (join-path dst-path dst-key)
          new-h (Hammock. src src-path dst dst-path anchors)]
      (h-fn new-h)))

  (-map! [this dst-key src-key h-fn]
    (let [src-path (join-path src-path src-key)
          dst-path (join-path dst-path dst-key)
          src-val (get-in src src-path)]
      (swap! dst assoc-in dst-path [])
      (dotimes [i (count src-val)]
        (let [src-path (join-path src-path i)
              dst-path (join-path dst-path i)
              new-h (Hammock. src src-path dst dst-path anchors)]
          (h-fn new-h)))))

  (-man! [this dst-key value src-keys]
    (let [dst-path (join-path dst-path dst-key)]
      (swap! dst assoc-in dst-path value)
      (doseq [k src-keys]
        (let [src-path (join-path src-path k)]
          (remember-anchors! src-path dst-path anchors)))))

  ILookup
  (-lookup [this k]
    (-lookup this k nil))
  (-lookup [this k not-found]
    (-lookup (get-in src src-path) k not-found)))

(defn create
  ([src]             (create src (atom {})))
  ([src dst]         (create src dst (atom {})))
  ([src dst anchors] (create src [] dst [] anchors))
  ([src src-path dst dst-path anchors]
   (let [src-path (norm-path src-path)
         dst-path (norm-path dst-path)]
     (Hammock. src src-path dst dst-path anchors))))

(defn copy!
  ([h dst-key src-key]
   (copy! h dst-key src-key identity))
  ([h dst-key src-key d-fn]
   (-copy! h dst-key src-key d-fn)))

(defn nest!
  [h dst-key src-key h-fn]
  (-nest! h dst-key src-key h-fn))

(defn map!
  [h dst-key src-key h-fn]
  (-map! h dst-key src-key h-fn))

(defn man!
  ([h dst-key value]
   (man! h dst-key value nil))
  ([h dst-key value src-keys]
   (-man! h dst-key value src-keys)))

(defn src-tree
  [h]
  (-src-tree h))

(defn dst-tree
  [h]
  (-dst-tree h))

(defn anchors
  [h]
  (-anchors h))
