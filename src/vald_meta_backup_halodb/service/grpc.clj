(ns vald-meta-backup-halodb.service.grpc
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [clojure.java.data :refer [from-java to-java]]
   [vald-meta-backup-halodb.handler.meta :as meta]
   [vald-meta-backup-halodb.handler.backup :as backup])
  (:import
   [io.grpc Server ServerBuilder]))

(defrecord GRPCComponent [options halodb]
  component/Lifecycle
  (start [this]
    (timbre/infof "Starting gRPC server...")
    (let [db (:db halodb)
          port (:port options)
          builder (ServerBuilder/forPort port)
          meta-service (meta/service db)
          backup-service (backup/service db)
          services (into [meta-service backup-service] (:services options))
          _ (doseq [s services]
              (.addService builder s))
          server (-> builder
                     (.build))]
      (timbre/infof "gRPC server started with port: %s" port)
      (-> server
          (.start))
      (assoc this :server server)))
  (stop [this]
    (timbre/infof "Stopping gRPC server...")
    (let [server (:server this)]
      (when server
        (-> server
            (.shutdown)))
      (assoc this :server nil))))

(defn start-grpc [options]
  (map->GRPCComponent
   {:options options}))
