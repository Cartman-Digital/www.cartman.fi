;; Local webserver definition. This class works by defining a local webserver. The server gets it's handler from stasis and jetty's middleware to get the right headers. Once the handler is ready. it is wrapped with reload and stacktrace functions from ring middleware. After all definitions starting the application can be done by running .start and .stop from the end of this file. 
(ns generator.webserver
  (:require
   [clojure.string :as string]
   [generator.builder :refer [generate]]
   [generator.notion-form :as contact] 
   [generator.pages :as pages]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.params :as ring.params]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [ring.middleware.reload :refer [wrap-reload]] 
   [ring.middleware.resource :refer [wrap-resource]] 
   [ring.middleware.stacktrace :refer [wrap-stacktrace]] 
   [ring.util.response :as response]
   [stasis.core :as stasis]
   [generator.contentful :as contentful]))

;; Handler for "/api/generate" endpoint WIP:
;; currently calling this causes the files to generate but browser triggers a file download.
;; caused by invalid returns?
(defn handle-generate
  [request]
  (System/getenv "")
  (generate)
  (response/header (response/response "") "Content-Type" "Text/html"))

(defn get-id-from-request
  "Returns the Contentful sys_id from api/preview/X urls"
  [request] 
  (let [params (ring.params/params-request request)
        {:strs [id]} (:query-params params)]
    id))

(defn handle-preview
  "Load and output a preview page"
  [request query content-type]
  (let [raw-response (contentful/get-contentful-preview query {:preview true 
                                                               :where {:sys {:id (get-id-from-request request)}}})
        data (first (get-in raw-response (if (= content-type :page) [:pageCollection :items] [:postCollection :items])))]
    (response/header 
     (response/response (pages/render-page data (if (= content-type :post) "post-page preview" "preview")))
     "Content-Type" "Text/html")))

(defn handle-contact
  [request]
  (let [parsed-request (ring.params/params-request request)
        {:strs [name email message g-recaptcha-response]} (:form-params parsed-request)] 
    (contact/process-submit {:name name
                             :email email
                             :message message
                             :g-captcha g-recaptcha-response}) 
    (response/header (response/response "Your message has been received. We will respond to you as soon as we can.") "Content-Type" "Text/html")))
  
;; Define the Jetty server handler
(defn wrap-api-routes [handler]
  (fn [request]
    (let [uri (:uri request)]
      (cond 
        (= uri "/api/generate") (handle-generate request)
        (= uri "/api/contact") (handle-contact request)
        (= uri "/api/ping") (response/header (response/response "pong\n") "Content-Type" "text/plain")
        (string/starts-with? uri "/api/preview/page") (handle-preview request :page-collection-query :page)
        (string/starts-with? uri "/api/preview/post") (handle-preview request :post-collection-query :post)
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
