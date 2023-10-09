(ns generator.webserver.preview
  (:require
   [generator.config :as config]
   [generator.contentful :as contentful]
   [generator.pages :as pages]
   [ring.middleware.params :as ring.params]
   [ring.util.response :as response]
   [taoensso.truss :as truss :refer [have?]]))

(defn request-id
  "Returns the Contentful sys_id from api/preview/X urls."
  [request]
  (let [params (ring.params/params-request request)
        {:strs [id]} (:query-params params)]
    (have? string? id)
    (have? not-empty id)
    id))

(defn validate-secret
  "Validates query parameter \"secret\" against CONTENTFUL_PREVIEW_SECRET config.
   Throws an error if they do not match."
  [request]
  (let [params (ring.params/params-request request)
        {:strs [secret]} (:query-params params)]
    (have? string? secret)
    (have? = (config/get-env "CONTENTFUL_PREVIEW_SECRET") secret)))

(defn page-data
  "Takes a type keyword in and returns a matching graphql keyword."
  [preview-type]
  (cond
    (= preview-type :post) {:query :post-collection-query 
                            :custom-html-classes "post-page"
                            :collection-name :postCollection}
    (= preview-type :page) {:query :page-collection-query
                            :custom-html-classes ""
                            :collection-name :pageCollection} 
    (= preview-type :person) {:query :person-collection-query
                            :custom-html-classes "person-page"
                            :collection-name :personCollection}
    :else (throw (ex-info "Invalid preview type" 
                          {:unrecognized-type preview-type}))))

(defn execute 
  "Takes a request and type of preview used. Returns html response for a preview of the page in question.
   Request must contain id and secret as query parameters"
  [request preview-type]
  (try
    (validate-secret request)
    (have? keyword? preview-type)
    (let [id (request-id request)
          query-keys (page-data preview-type)
          raw-data (contentful/get-contentful-preview (:query query-keys) {:preview true
                                                                           :where {:sys {:id id}}})
          data (first (get-in raw-data [(:collection-name query-keys) :items]))]
      (response/header
       (response/response (pages/render-page data (:custom-html-classes query-keys)))
       "Content-Type" "Text/html"))
   (catch clojure.lang.ExceptionInfo exception
     (prn (.getMessage exception))
     (-> (response/response "<h1>Invalid token</h1>")
         (response/status 400)
         (response/header "Content-Type" "Text/html")))))
