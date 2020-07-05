(ns vald-meta-backup-halodb.model.common
  (:require
   [clojure.java.data :refer [to-java from-java]])
  (:import
   [org.vdaas.vald.payload Empty]
   [org.vdaas.vald.payload Info$IPs]))

(defmethod to-java [Empty clojure.lang.APersistentMap] [_ m]
  (-> (Empty/newBuilder)
      (.build)))

(defmethod from-java Empty [^Empty obj]
  {})

(defmethod to-java [Info$IPs clojure.lang.APersistentVector] [_ ips]
  (let [builder (Info$IPs/newBuilder)]
    (doseq [ip ips]
      (-> builder
          (.addIp ip)))
    (-> builder
        (.build))))

(defmethod from-java Info$IPs [^Info$IPs obj]
  (let [len (.getIpCount obj)]
    (mapv (fn [i]
            (-> obj
                (.getIp i))) (range len))))
