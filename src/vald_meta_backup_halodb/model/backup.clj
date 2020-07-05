(ns vald-meta-backup-halodb.model.backup
  (:require
   [clojure.java.data :refer [to-java from-java]])
  (:import
   [org.vdaas.vald.payload Backup$GetVector$Request]
   [org.vdaas.vald.payload Backup$GetVector$Owner]
   [org.vdaas.vald.payload Backup$Locations$Request]
   [org.vdaas.vald.payload Backup$Compressed$MetaVector]
   [org.vdaas.vald.payload Backup$Compressed$MetaVectors]
   [org.vdaas.vald.payload Backup$Remove$Request]
   [org.vdaas.vald.payload Backup$Remove$RequestMulti]
   [org.vdaas.vald.payload Backup$IP$Register$Request]
   [org.vdaas.vald.payload Backup$IP$Remove$Request]))

(defmethod to-java [Backup$GetVector$Request clojure.lang.APersistentMap] [_ m]
  (-> (Backup$GetVector$Request/newBuilder)
      (.setUuid (:uuid m))
      (.build)))

(defmethod from-java Backup$GetVector$Request [^Backup$GetVector$Request obj]
  {:uuid (.getUuid obj)})

(defmethod to-java [Backup$GetVector$Owner clojure.lang.APersistentMap] [_ m]
  (-> (Backup$GetVector$Owner/newBuilder)
      (.setIp (:ip m))
      (.build)))

(defmethod from-java Backup$GetVector$Owner [^Backup$GetVector$Owner obj]
  {:ip (.getIp obj)})

(defmethod to-java [Backup$Locations$Request clojure.lang.APersistentMap] [_ m]
  (-> (Backup$Locations$Request/newBuilder)
      (.setUuid (:uuid m))
      (.build)))

(defmethod from-java Backup$Locations$Request [^Backup$Locations$Request obj]
  {:uuid (.getUuid obj)})


(defmethod to-java [Backup$Compressed$MetaVector clojure.lang.APersistentMap] [_ m]
  (let [builder (-> (Backup$Compressed$MetaVector/newBuilder)
                    (.setUuid (:uuid m))
                    (.setMeta (:meta m)))]
    (doseq [fl (:vector m)]
      (-> builder
          (.addVector fl)))
    (doseq [ip (:ips m)]
      (-> builder
          (.addIp ip)))
    (-> builder
        (.build))))

(defmethod from-java Backup$Compressed$MetaVector [^Backup$Compressed$MetaVector obj]
  {:uuid (.getUuid obj)
   :meta (.getMeta obj)
   :vector (mapv (fn [i]
                   (-> obj
                       (.getVector i))) (range (.getVectorCount obj)))
   :ips (mapv (fn [i]
                (-> obj
                    (.getIps i))) (range (.getIpsCount obj)))})

(defmethod to-java [Backup$Compressed$MetaVectors clojure.lang.APersistentVector] [_ mvs]
  (let [builder (Backup$Compressed$MetaVectors/newBuilder)]
    (doseq [mv mvs]
      (-> builder
          (.addVectors (to-java mv))))
    (-> builder
        (.build))))

(defmethod from-java Backup$Compressed$MetaVectors [^Backup$Compressed$MetaVectors obj]
  (mapv (fn [i]
          (-> obj
              (.getVectors i)
              (from-java))) (range (.getVectorsCount obj))))

(defmethod to-java [Backup$Remove$Request clojure.lang.APersistentMap] [_ m]
  (-> (Backup$Remove$Request/newBuilder)
      (.setUuid (:uuid m))
      (.build)))

(defmethod from-java Backup$Remove$Request [^Backup$Remove$Request obj]
  {:uuid (.getUuid obj)})

(defmethod to-java [Backup$Remove$RequestMulti clojure.lang.APersistentVector] [_ uuids]
  (let [builder (Backup$Remove$RequestMulti/newBuilder)]
    (doseq [uuid uuids]
      (-> builder
          (.addUuids uuid)))
    (-> builder
        (.build))))

(defmethod from-java Backup$Remove$RequestMulti [^Backup$Remove$RequestMulti obj]
  (mapv (fn [i]
          (-> obj
              (.getUuids i))) (range (.getUuidsCount obj))))

(defmethod to-java [Backup$IP$Register$Request clojure.lang.APersistentMap] [_ m]
  (let [builder (-> (Backup$IP$Register$Request/newBuilder)
                    (.setUuid (:uuid m)))]
    (doseq [ip (:ips m)]
      (-> builder
          (.addIps ip)))
    (-> builder
        (.build))))

(defmethod from-java Backup$IP$Register$Request [^Backup$IP$Register$Request obj]
  {:uuid (.getUuid obj)
   :ips (mapv (fn [i]
                (-> obj
                    (.getIps i))) (range (.getIpsCount obj)))})

(defmethod to-java [Backup$IP$Remove$Request clojure.lang.APersistentVector] [_ ips]
  (let [builder (Backup$IP$Remove$Request/newBuilder)]
    (doseq [ip ips]
      (-> builder
          (.addIps ip)))
    (-> builder
        (.build))))

(defmethod from-java Backup$IP$Remove$Request [^Backup$IP$Remove$Request obj]
  (mapv (fn [i]
          (-> obj
              (.getIps i))) (range (.getIpsCount obj))))
