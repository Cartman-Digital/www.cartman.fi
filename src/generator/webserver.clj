;; Local webserver definition. This class works by defining a local webserver. The server gets it's handler from stasis and jetty's middleware to get the right headers. Once the handler is ready. it is wrapped with reload and stacktrace functions from ring middleware. After all definitions starting the application can be done by running .start and .stop from the end of this file. 
(ns generator.webserver
  (:require
   [clojure.string :as string] 
   [generator.pages :as pages]
   [generator.webserver.build :as build]
   [generator.webserver.contact :as contact]
   [generator.webserver.preview :as preview]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]] 
   [ring.middleware.refresh :refer [wrap-refresh]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.stacktrace :refer [wrap-stacktrace]]
   [ring.util.response :as response]
   [stasis.core :as stasis]))
 
;; Define the Jetty server handler
(defn wrap-api-routes [handler]
  (fn [request]
    (let [uri (:uri request)]
      (cond 
        (= uri "/api/generate") (build/execute request)
        (= uri "/api/contact") (contact/execute request)
        (= uri "/api/ping") (response/header (response/response "pong\n") "Content-Type" "text/plain")
        (string/starts-with? uri "/api/preview/page") (preview/execute request :page)
        (string/starts-with? uri "/api/preview/post") (preview/execute request :post)
        (string/starts-with? uri "/assets/v/") (response/redirect (clojure.string/replace-first uri #"v/[0-9]*/" ""))
        :else (handler request)))))

(def app (-> (stasis/serve-pages pages/get-pages) ;; should return map of slug -> render call
             (wrap-resource "public")
             (wrap-api-routes)
             (wrap-content-type)
             (wrap-not-modified) 
             (wrap-refresh)
             (wrap-reload {:dirs ["src/generator" "resources/public"]})
             (wrap-stacktrace)))

(defonce server (atom nil))

(defn start-webserver []
  (when (nil? @server)
    (reset! server (jetty/run-jetty #'app {:port 8000 :join? false}))
    (println "Web server started.")))

(defn stop-webserver []
  (when-not (nil? @server)
    (.stop @server)
    (reset! server nil)
    (println "Web server stopped.")))

(comment
 (start-webserver)
 (stop-webserver)
 (pages/get-pages))
