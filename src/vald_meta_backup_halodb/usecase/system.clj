(ns vald-meta-backup-halodb.usecase.system
  (:require
   [com.stuartsierra.component :as component]
   [vald-meta-backup-halodb.service.server :as server]
   [vald-meta-backup-halodb.service.grpc :as grpc]
   [vald-meta-backup-halodb.service.halodb :as halodb]))

(defn system [{:keys [grpc liveness readiness] :as conf}]
  (component/system-map
   :liveness (server/start-server liveness)
   :db (halodb/start-halodb
         {:dir ".halodb"
          :config {:max-file-size 131072
                   :sync-write true}})
   :grpc (component/using
          (grpc/start-grpc grpc)
          {:liveness :liveness
           :halodb :db})
   :readiness (component/using
               (server/start-server readiness)
               {:grpc :grpc})))
