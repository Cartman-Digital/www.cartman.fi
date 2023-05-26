;; Local webserver definition. This class works by defining a local webserver. The server gets it's handler from stasis and jetty's middleware to get the right headers. Once the handler is ready. it is wrapped with reload and stacktrace functions from ring middleware. After all definitions starting the application can be done by running .start and .stop from the end of this file. 
(ns generator.webserver
  (:require
   [generator.pages :refer [get-pages]]
   [generator.builder :refer [generate]]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.stacktrace :refer [wrap-stacktrace]] 
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.util.response :as response]
   [stasis.core :as stasis]))

;; Handler for "/api/generate" endpoint WIP:
;; currently calling this causes the files to generate but browser triggers a file download.
;; caused by invalid returns?
(defn handle-generate
  [request]
  (System/getenv "")
  (generate)
  (response/header (response/response "") "Content-Type" "Text/html"))

;; Define the Jetty server handler
(defn wrap-api-routes [handler]
  (fn [request]
    (let [uri (:uri request)]
      (cond 
        (= uri "/api/generate") (handle-generate request)
        (= uri "/api/ping") (response/header (response/response "pong") "Content-Type" "text/plain")
        :else (handler request)))))

(def app (-> (stasis/serve-pages get-pages) ;; should return map of slug -> render call
             (wrap-resource "public")
             (wrap-api-routes)
             (wrap-content-type)
             (wrap-not-modified)
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

(comment (start-webserver))
(comment (stop-webserver))
