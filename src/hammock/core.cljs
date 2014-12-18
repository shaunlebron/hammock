(ns hammock.core)

(defprotocol IHammock
  (-src [h])
  (-dst [h])
  (-anchors [h])
  (-set! [h dst-key src-key]
         [h dst-key src-key d-fn])
  (-nest! [h dst-key src-key]
          [h dst-key src-key h-fn])
  (-map! [h dst-key src-key]
         [h dst-key src-key h-fn])
  (-man! [h dst-ks value]
         [h dst-ks value src-keys]))

(deftype Hammock [src dst anchors]
  ILookup
  (-lookup [this k]
    (-lookup this k nil))
  (-lookup [this k not-found]
    (-lookup src k not-found)))

(defn create
  ([src] (create src (atom {}) (atom {})))
  ([src dst] (create src dst (atom {})))
  ([src dst anchors]
   (Hammock. src dst anchors)))

;; (set! h :dest-key :src-key fn?)
;; (nest! h :dest-key :src-key h-fn)
;; (map! h :dest-key :src-key h-fn)
;; (man! h :dest-key fn :src-keys)
