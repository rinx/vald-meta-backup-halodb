(ns vald-meta-backup-halodb.service.server
  (:require
   [clojure.java.io :as io]
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [compojure.core :as compojure]
   [compojure.route :as route]
   [org.httpkit.server :as server]))

(defn handler-fn [body]
  (fn [req]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body body}))

(defn ->route [r opts]
  (let [route (:route r)
        body-file (when (and (:body-file r)
                             (-> (io/file (:body-file r))
                                 (.exists)))
                    (slurp (:body-file r)))
        body (:body r)
        response (or body-file body)
        handler (handler-fn response)]
    (when (and route handler)
      (timbre/debugf "Registering route '%s'", route)
      (compojure/make-route :get route handler))))

(defn routes->router [routes opts]
  (->> routes
       (map #(->route % opts))
       (filter some?)
       (cons (route/not-found (slurp (io/resource "not-found.html"))))
       (reverse)
       (apply compojure/routes)))

(defrecord ServerComponent [options]
  component/Lifecycle
  (start [this]
    (let [server-name (:name options)
          routes (:routes options)
          opts {}]
      (timbre/infof "Starting server: %s..." server-name)
      (let [router (routes->router routes opts)
            server (server/run-server router options)
            port (:port options)]
        (timbre/infof "Server %s started with port: %s" server-name port)
        (timbre/infof "Starting server %s completed." server-name)
        (assoc this :server server))))
  (stop [this]
    (let [server-name (:name options)]
      (timbre/infof "Stopping server: %s..." server-name)
      (let [server (:server this)
            timeout (:prestop-duration options)]
        (when server
          (server :timeout timeout)))
      (timbre/infof "Stopping server %s completed." server-name)
      (assoc this :server nil))))

(defn start-server [options]
  (map->ServerComponent
   {:options options}))
