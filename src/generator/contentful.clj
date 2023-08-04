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
(def contentful-environment "master")
(def contentful-url (str "https://graphql.contentful.com/content/v1/spaces/" 
                         contentful-space 
                         "/environments/" 
                         contentful-environment))
(def headers {:Authorization (str "Bearer " (get-env "CONTENTFUL_TOKEN"))})

(declare graphql-queries)

;; defgraphql does not support queries that start with graphql comments.
(defgraphql graphql-queries "contentful/getNavTree.graphql"
  "contentful/getPages.graphql"
  "contentful/getAsset.graphql"
  "contentful/getCardList.graphql"
  "contentful/getSitemap.graphql"
  "contentful/getEntry.graphql"
  "contentful/getPostCollection.graphql"
  "contentful/getPostList.graphql"
  "contentful/getPostsByList.graphql")
(def query-map (graphql-core/query-map graphql-queries))

(defn ^:private api-call
  [query]
  (let [graphql (have map? (:graphql query))]
    (->> (client/post contentful-url {:accept :json
                                      :debug false
                                      :body (json/write-str {:query (:query graphql)
                                                             :variables (:variables graphql)})
                                      :headers headers
                                      :throw-entire-message? true
                                      :content-type :json})
         (m/decode-response-body)
         (:data))))

(def ^:private dispatch 
  (memo/memo api-call))

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


(comment (get-contentful :nav-collection-query {:name "Main menu"}))
(comment (get-contentful :asset-query {:$assetId "34YRcoaS4WJ5ORhpMlMHRM"}))
(comment (get-contentful :entry-query {:entryId "782ka3lNsGXBrnE88Qf3jt"}))
(comment (get-contentful :card-list-query {:listId "2pJ63nhY2QKbVesxgWOvq9"}))
(comment (get-contentful :post-collection-query {:type ["news" "dev"]}))
(comment (json/write-str (:graphql ((get-in query-map [:query :asset-query]) {:assetId "34YRcoaS4WJ5ORhpMlMHRM"}))))
(comment (:variables (:graphql ((get-in query-map [:query :nav-collection-query]) {:name "Main menu"}))))
(comment (memo/memo-clear! api-call)) ; evaluate this in REPL to clear memoize cache from api-call this allows you to update page content from contentful without restart 
