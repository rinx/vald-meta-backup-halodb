(ns vald-meta-backup-halodb.model.meta
  (:require
   [clojure.java.data :refer [to-java from-java]])
  (:import
   [org.vdaas.vald.payload Meta$Key Meta$Val Meta$Keys Meta$Vals]
   [org.vdaas.vald.payload Meta$KeyVal Meta$KeyVals]))

(defmethod to-java [Meta$Key clojure.lang.APersistentMap] [_ m]
  (-> (Meta$Key/newBuilder)
      (.setKey (:key m))
      (.build)))

(defmethod from-java Meta$Key [^Meta$Key obj]
  {:key (.getKey obj)})

(defmethod to-java [Meta$Val clojure.lang.APersistentMap] [_ m]
  (-> (Meta$Val/newBuilder)
      (.setVal (:val m))
      (.build)))

(defmethod from-java Meta$Val [^Meta$Val obj]
  {:val (.getVal obj)})

(defmethod to-java [Meta$Keys clojure.lang.APersistentVector] [_ ks]
  (let [builder (Meta$Keys/newBuilder)]
    (doseq [k ks]
      (-> builder
          (.addKeys k)))
    (-> builder
        (.build))))

(defmethod from-java Meta$Keys [^Meta$Keys obj]
  (let [len (.getKeysCount obj)]
    (mapv (fn [i]
            (-> obj
                (.getKeys i))) (range len))))

(defmethod to-java [Meta$Vals clojure.lang.APersistentVector] [_ vs]
  (let [builder (Meta$Vals/newBuilder)]
    (doseq [v vs]
      (-> builder
          (.addVals v)))
    (-> builder
        (.build))))

(defmethod from-java Meta$Vals [^Meta$Vals obj]
  (let [len (.getValsCount obj)]
    (mapv (fn [i]
            (-> obj
                (.getVals i))) (range len))))

(defmethod to-java [Meta$KeyVal clojure.lang.APersistentMap] [_ m]
  (-> (Meta$KeyVal/newBuilder)
      (.setKey (:key m))
      (.setVal (:val m))
      (.build)))

(defmethod from-java Meta$KeyVal [^Meta$KeyVal obj]
  {:key (.getKey obj)
   :val (.getVal obj)})

(defmethod to-java [Meta$KeyVals clojure.lang.APersistentVector] [_ kvs]
  (let [builder (Meta$KeyVals/newBuilder)]
    (doseq [kv kvs]
      (-> builder
          (.addKvs kv)))
    (-> builder
        (.build))))

(defmethod from-java Meta$KeyVals [^Meta$KeyVals obj]
  (let [len (.getKvsCount obj)]
    (mapv (fn [i]
            (-> obj
                (.getKvs i)
                (from-java))) (range len))))
