(ns vald-meta-backup-halodb.model.grpc
  (:import
   [io.grpc Status]))

(defn not-found
  ([]
   (-> (Status/NOT_FOUND)
       (.asException)))
  ([e]
   (-> (Status/NOT_FOUND)
       (.withCause e)
       (.asException))))
