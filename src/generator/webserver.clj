(ns generator.webserver
  (:require [ring.adapter.jetty :as ring-jetty]
            [reitit.ring :as ring]
            [ring.util.response :as r]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.spec :as rs]
            [generator.pages :as pages]))

(defn handler [slug]
  {:status 200 :body (pages/render-page slug)})

(def app
  (ring/ring-handler
   (ring/router
    ["/" ; TODO aside from the default url, the rest need to be fetched from contentful.
     ["" {:handler handler}]]
    {:data {:muuntaja m/instance
            :middleware [muuntaja/format-middleware]}
     :validate rs/validate})))

(defn start []
  (ring-jetty/run-jetty #'app {:port  3001
                               :join? false}))

(def server (start))

(.stop server)