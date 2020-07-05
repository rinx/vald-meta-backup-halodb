(ns vald-meta-backup-halodb.handler.meta
  (:require
   [taoensso.timbre :as timbre]
   [clojure.java.data :refer [from-java to-java]]
   [clj-halodb.core :as halodb]
   [vald-meta-backup-halodb.model.meta]
   [vald-meta-backup-halodb.model.common])
  (:import
   [org.vdaas.vald.meta MetaGrpc$MetaImplBase]
   [org.vdaas.vald.payload Meta$Key Meta$Val Meta$Keys Meta$Vals]
   [org.vdaas.vald.payload Meta$KeyVal Meta$KeyVals Empty]))

(defn kv-id [k]
  (str "kv-" k))

(defn vk-id [v]
  (str "vk-" v))

(defn get-v [db k]
  (halodb/get db (kv-id k)))

(defn get-k [db v]
  (halodb/get db (vk-id v)))

(defn put-kv! [db k v]
  (doall
    (halodb/put db {(kv-id k) v
                    (vk-id v) k})))

(defn delete-kv! [db k v]
  (halodb/delete db (kv-id k))
  (halodb/delete db (vk-id v)))

(defn get-meta [db ^Meta$Key k]
  (let [k (:key (from-java k))
        v (get-v db k)]
    (if v
      (to-java Meta$Val {:val v})
      (throw (Exception. "not found")))))

(defn get-metas [db ^Meta$Keys ks]
  (let [ks (from-java ks)]
    (->> ks
         (map #(get-v db %))
         (to-java Meta$Vals))))

(defn get-meta-inverse [db ^Meta$Val v]
  (let [v (:val (from-java v))
        k (get-k db v)]
    (if k
      (to-java Meta$Key {:key k})
      (throw (Exception. "not found")))))

(defn get-metas-inverse [db ^Meta$Vals vs]
  (let [vs (from-java vs)]
    (->> vs
         (map #(get-k db %))
         (to-java Meta$Vals))))

(defn set-meta [db ^Meta$KeyVal kv]
  (let [kv (from-java kv)
        k (:key kv)
        v (:val kv)]
    (put-kv! db k v)
    (to-java Empty {})))

(defn set-metas [db ^Meta$KeyVals kvs]
  (let [kvs (from-java kvs)]
    (->> kvs
         (map (fn [kv]
                (put-kv! db (:key kv) (:val kv)))))
    (to-java Empty {})))

(defn delete-meta [db ^Meta$Key k]
  (let [k (:key (from-java k))
        v (get-v db k)]
    (delete-kv! db k v)
    (to-java Meta$Val {:val v})))

(defn delete-metas [db ^Meta$Keys ks]
  )

(defn delete-meta-inverse [db ^Meta$Val v]
  (let [v (:val (from-java v))
        k (get-k db v)]
    (delete-kv! db k v)
    (to-java Meta$Key {:key k})))

(defn delete-metas-inverse [db ^Meta$Vals vs]
  )

(defn service [db]
  (letfn [(>> [f arg response]
            (try
              (->> (f arg)
                   (.onNext response))
              (.onCompleted response)
              (catch Throwable e
                (.onError response e))))]
    (proxy [MetaGrpc$MetaImplBase] []
      (getMeta [^Meta$Key k response]
        (>> #(get-meta db %) k response))
      (getMetas [^Meta$Keys ks response]
        (>> #(get-metas db %) ks response))
      (getMetaInverse [^Meta$Val v response]
        (>> #(get-meta-inverse db %) v response))
      (getMetasInverse [^Meta$Vals vs response]
        (>> #(get-metas-inverse db %) vs response))
      (setMeta [^Meta$KeyVal kv response]
        (>> #(set-meta db %) kv response))
      (setMetas [^Meta$KeyVals kvs response]
        (>> #(set-metas db %) kvs response))
      (deleteMeta [^Meta$Key k response]
        (>> #(delete-meta db %) k response))
      (deleteMetas [^Meta$Keys ks response]
        (>> #(delete-metas db %) ks response))
      (deleteMetaInverse [^Meta$Val v response]
        (>> #(delete-meta-inverse db %) v response))
      (deleteMetasInverse [^Meta$Vals vs response]
        (>> #(delete-metas-inverse db %) vs response)))))
