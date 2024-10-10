(ns generator.contentful
  (:require
   [clj-http.client :as client]
   [clojure.data.json :as json]
   [clojure.core.memoize :as memo] 
   [generator.config :refer [get-env]]
   [graphql-builder.parser :refer [defgraphql]]
   [graphql-builder.core :as graphql-core]
   [muuntaja.core :as m]
   [taoensso.truss :as truss :refer (have)]))

(def contentful-space (get-env "CONTENTFUL_SPACE"))
(def contentful-environment (get-env "CONTENTFUL_ENVIRONMENT"))
(def contentful-url (str "https://graphql.contentful.com/content/v1/spaces/" 
                         contentful-space 
                         "/environments/" 
                         contentful-environment))

(def api-url
  (str "https://cdn.contentful.com/spaces/"
       contentful-space
       "/environments/"
       contentful-environment
       "/entries"))

(def headers {:Authorization (str "Bearer " (get-env "CONTENTFUL_TOKEN"))})
(def preview-headers {:Authorization (str "Bearer " (get-env "CONTENTFUL_PREVIEW_TOKEN"))})

(declare graphql-queries)

;; defgraphql does not support queries that start with graphql comments.
;; Debugging tips:
;;  - If you encounter Macroexpanding errors while evaluating this expression check your graphql for errors.
;;  - Please try to avoid "Boolean = false" variable declarations in graphql queries. 
;;    They do not work as intended and send NULL to contentful instead!
(defgraphql graphql-queries "contentful/getNavTree.graphql"
  "contentful/getPages.graphql"
  "contentful/getAsset.graphql"
  "contentful/getCardList.graphql"
  "contentful/getSitemap.graphql"
  "contentful/getEntry.graphql"
  "contentful/getPostCollection.graphql" ; Post Collection is used by generator to create full post pages
  "contentful/getPostsByList.graphql" ; Posts by list is used by contentful widgets that load articles on any page.
  "contentful/getLastPostPublishDate.graphql"
  "contentful/getPersonList.graphql"
  "contentful/getPersonCollection.graphql");Person Collection used for personel page
(def query-map (graphql-core/query-map graphql-queries))

(defn ^:private api-call
  [query & [preview?]]
  (let [graphql (have map? (:graphql query))
        json (json/write-str {:query (:query graphql)
                              :variables (:variables graphql)})]
    (->> (client/post contentful-url {:accept :json
                                      :debug false
                                      :body json
                                      :headers (if preview? preview-headers headers) ; preview content from graphql requires a different authorization token to view it.
                                      :throw-entire-message? true
                                      :content-type :json})
         (m/decode-response-body)
         (:data))))

(def ^:private dispatch 
  (memo/memo api-call))

(defn get-contentful-preview
  "Returns Page or Post preview from contentful. Preview content is"
  ([query gql-filter]
   (have keyword? query)
   (have map? gql-filter)
   (let [graphql-fn (have fn? (get-in query-map [:query query]))
         graphql    (graphql-fn gql-filter)]
     (api-call graphql true))))

(defn get-contentful
  "get Contentful entities"
  ([query gql-filter]
   (have keyword? query)
   (have map? gql-filter)
   (let [graphql-fn (have fn? (get-in query-map [:query query]))
         graphql    (graphql-fn gql-filter)]
     (dispatch graphql)))
  ([query]
   (have keyword? query)
   (let [graphql-fn (have fn? (get-in query-map [:query query]))
         graphql    (graphql-fn)]
     (dispatch graphql))))

(defn get-item-by-slug [content-type slug]
  (let [params {:fields.slug slug
                :content_type content-type
                :include 10}
        url (reduce (fn [url [k v]]
                      (str url "&" (name k) "=" (str v)))
                    (str api-url "?") params)
        data (->> (client/get url {:headers headers}) :body json/read-json)]
    {:items (-> data :items first)
     :assets (-> data :includes :Asset)
     :entries (-> data :includes :Entry)}))

(defn get-image-by-slug [slug]
  (if-let [data (get-item-by-slug "image" slug)]
    (let [id (-> data :items :fields :image :sys :id)]
      (->> data
        :assets
        (filter #(= (-> % :sys :id) id))
        first :fields :file :url))))

(comment (get-contentful :nav-collection-query {:name "Main menu"}))
(comment (get-contentful :asset-query {:assetId "34YRcoaS4WJ5ORhpMlMHRM"}))
(comment (get-contentful :entry-query {:entryId "782ka3lNsGXBrnE88Qf3jt"}))
(comment (get-contentful :card-list-query {:listId "2pJ63nhY2QKbVesxgWOvq9"}))
(comment (get-contentful :post-collection-query {:type ["news" "dev" "article"]}))
(comment (json/write-str (:graphql ((get-in query-map [:query :asset-query]) {:assetId "34YRcoaS4WJ5ORhpMlMHRM"}))))
(comment (:variables (:graphql ((get-in query-map [:query :nav-collection-query]) {:name "Main menu"}))))
(comment (get-contentful :person-collection-query))
(comment (get-contentful :post-list-query))
(comment (get-contentful :people-by-list-query {:listId "3KpjOQlZHffoJCACpIGnvh"}))
(comment (memo/memo-clear! api-call)) ; evaluate this in REPL to clear memoize cache from api-call this allows you to update page content from contentful without restart 

