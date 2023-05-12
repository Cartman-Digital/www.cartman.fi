;; Local webserver definition. This class works by defining a local webserver. The server gets it's handler from stasis with assets wrapped by optimus and jetty's middleware to get the right headers. Once the handler is ready. it is wrapped with reload and stacktrace functions from ring middleware. After all definitions starting the application can be done by running .start and .stop from the end of this file. 
(ns generator.webserver
  (:require
   [stasis.core :as stasis]
   [optimus.prime :as optimus]
   [optimus.assets :as assets]
   [optimus.optimizations :as optimizations]
   [optimus.strategies :refer [serve-live-assets]]
   [optimus.export]
   [generator.pages :refer [get-pages]]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.stacktrace :refer [wrap-stacktrace]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]))

(defn get-assets []
  (assets/load-assets "public" ["/assets/main.css" 
                                #".*\.(jpg|svg|png|js|xml)$"]))

(def app (-> (stasis/serve-pages get-pages) ;; should return map of slug -> render call
             (optimus/wrap get-assets optimizations/all serve-live-assets)
             (wrap-content-type)
             (wrap-not-modified)
             (wrap-reload {:dirs ["src/generator" "resources/public"]})
             (wrap-stacktrace)))

;; defines the local server to start by default the server is accessible from http://localhost:8000 after running .start on the server from comments below
(defonce server
  (jetty/run-jetty #'app {:port 8000 :join? false}))
  
(comment (.start server))
(comment (.stop server))

(comment 
  (get-pages))