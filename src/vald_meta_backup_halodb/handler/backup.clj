(ns vald-meta-backup-halodb.handler.backup
  (:require
   [taoensso.timbre :as timbre]
   [clojure.java.data :refer [from-java to-java]]
   [clj-halodb.core :as halodb]
   [vald-meta-backup-halodb.model.backup]
   [vald-meta-backup-halodb.model.common]
   [vald-meta-backup-halodb.model.grpc :as grpc])
  (:refer-clojure :exclude [remove])
  (:import
   [org.vdaas.vald.manager.backup BackupGrpc$BackupImplBase]
   [org.vdaas.vald.payload Backup$GetVector$Request]
   [org.vdaas.vald.payload Backup$Locations$Request]
   [org.vdaas.vald.payload Backup$Compressed$MetaVector]
   [org.vdaas.vald.payload Backup$Compressed$MetaVectors]
   [org.vdaas.vald.payload Backup$Remove$Request]
   [org.vdaas.vald.payload Backup$Remove$RequestMulti]
   [org.vdaas.vald.payload Backup$IP$Register$Request]
   [org.vdaas.vald.payload Backup$IP$Remove$Request]
   [org.vdaas.vald.payload Empty Info$IPs]))

(defn mv-id [k]
  (str "mv-" k))

(defn get-mv [db k]
  (halodb/get db (mv-id k)))

(defn put-mv! [db k mv]
  (doall
    (halodb/put db {(mv-id k) mv})))

(defn delete-mv! [db k]
  (halodb/delete db (mv-id k)))


(defn get-vector [db ^Backup$GetVector$Request req]
  (let [uuid (:uuid (from-java req))
        mv (get-mv db uuid)]
    (if mv
      (to-java Backup$Compressed$MetaVector mv)
      (throw
       (grpc/not-found (Exception. "not found"))))))

(defn locations [db ^Backup$Locations$Request req]
  (let [uuid (:uuid (from-java req))
        mv (get-mv db uuid)]
    (if mv
      (to-java Info$IPs (:ips mv))
      (throw
       (grpc/not-found (Exception. "not found"))))))

(defn register [db ^Backup$Compressed$MetaVector mv]
  (let [mv (from-java mv)
        uuid (:uuid mv)]
    (put-mv! db uuid mv)
    (to-java Empty {})))

(defn register-multi [db ^Backup$Compressed$MetaVectors mvs]
  )

(defn remove [db ^Backup$Remove$Request req]
  (let [uuid (:uuid (from-java req))]
    (delete-mv! db uuid)
    (to-java Empty {})))

(defn remove-multi [db ^Backup$Remove$RequestMulti req]
  )

(defn register-ips [db ^Backup$IP$Register$Request req]
  (let [req (from-java req)
        uuid (:uuid req)
        ips (:ips req)
        mv (get-mv db uuid)
        mv' (update mv :ips into ips)]
    (put-mv! db uuid mv')
    (to-java Empty {})))

(defn remove-ips [db ^Backup$IP$Remove$Request req]
  )


(defn service [db]
  (letfn [(>> [f arg response]
            (try
              (->> (f arg)
                   (.onNext response))
              (.onCompleted response)
              (catch Throwable e
                (.onError response e))))]
    (proxy [BackupGrpc$BackupImplBase] []
      (getVector [^Backup$GetVector$Request req response]
        (>> #(get-vector db %) req response))
      (locations [^Backup$Locations$Request req response]
        (>> #(locations db %) req response))
      (register [^Backup$Compressed$MetaVector mv response]
        (>> #(register db %) mv response))
      (registerMulti [^Backup$Compressed$MetaVectors mvs response]
        (>> #(register-multi db %) mvs response))
      (remove [^Backup$Remove$Request req response]
        (>> #(remove db %) req response))
      (removeMulti [^Backup$Remove$RequestMulti req response]
        (>> #(remove-multi db %) req response))
      (registerIPs [^Backup$IP$Register$Request req response]
        (>> #(register-ips db %) req response))
      (removeIPs [^Backup$IP$Remove$Request req response]
        (>> #(remove-ips db %) req response)))))
