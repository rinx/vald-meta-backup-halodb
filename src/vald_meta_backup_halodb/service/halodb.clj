(ns vald-meta-backup-halodb.service.halodb
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [clj-halodb.core :as halodb]))

(System/setProperty "org.caffinitas.ohc.allocator" "unsafe")

(defrecord HaloDBComponent [options]
  component/Lifecycle
  (start [this]
    (timbre/infof "Starting HaloDB...")
    (let [dir (:dir options)
          halodb-options (halodb/options (:config options))
          db (halodb/open dir halodb-options)]
      (timbre/debugf "the number of stored data: %s" (halodb/size db))
      (assoc this :db db)))
  (stop [this]
    (timbre/infof "Stopping HaloDB...")
    (let [db (:db this)]
      (when db
        (halodb/close db))
      (assoc this :db nil))))

(defn start-halodb [options]
  (map->HaloDBComponent
   {:options options}))
