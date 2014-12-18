(ns hammock.core)

(defprotocol IHammock
  (-copy! [this dst-key src-key d-fn])
  (-nest! [this dst-key src-key]
          [this dst-key src-key h-fn])
  (-map! [this dst-key src-key]
         [this dst-key src-key h-fn])
  (-man! [this dst-ks value]
         [this dst-ks value src-keys]))

(defn- normalize-key
  [k]
  (if (sequential? k) k [k]))

(defn- create-missing-levels!
  [dst dst-path]
  nil)

(defn- remember-anchors!
  [src-path dst-path anchors]
  nil)

(deftype Hammock [src src-path dst dst-path anchors]
  IHammock
  (-copy! [this dst-key src-key d-fn]
    (let [dst-key (normalize-key dst-key)
          src-key (normalize-key src-key)
          src-path (concat src-path src-key)
          src-val (d-fn (get-in src src-path))
          dst-path (concat dst-path dst-key)]
      (create-missing-levels! dst dst-path)
      (swap! dst assoc-in dst-path src-val)
      (remember-anchors! src-path dst-path anchors)))

  ILookup
  (-lookup [this k]
    (-lookup this k nil))
  (-lookup [this k not-found]
    (-lookup src k not-found)))

(defn create
  ([src]             (create src (atom {})))
  ([src dst]         (create src dst (atom {})))
  ([src dst anchors] (create src [] dst [] anchors))
  ([src src-path dst dst-path anchors]
   (Hammock. src src-path dst dst-path anchors)))

(defn copy!
  ([h dst-key src-key]
   (copy! h dst-key src-key identity))
  ([h dst-key src-key d-fn]
   (-copy! h dst-key src-key d-fn)))

;; (nest! h :dest-key :src-key h-fn)
;; (map! h :dest-key :src-key h-fn)
;; (man! h :dest-key fn :src-keys)
