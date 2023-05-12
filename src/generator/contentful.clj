(ns generator.contentful
  (:require
   [taoensso.truss :as truss :refer (have)]
   [graphql-builder.parser :refer [defgraphql]] 
   [graphql-builder.core :as graphql-core]
   [clj-http.client :as client] 
   [muuntaja.core :as m]
   [clojure.data.json :as json]
   [clojure.core.memoize :refer [memo-clear!]]))
   

(def contentful-space "038s6vr0kmv0")
(def contentful-environment "master")
(def contentful-url (str "https://graphql.contentful.com/content/v1/spaces/" 
                         contentful-space 
                         "/environments/" 
                         contentful-environment))
(def headers {:Authorization "Bearer QJSIZZ8-mkUZPBySwQTtVwfirskzh2ZnplerHDo6xpE"})

(declare graphql-queries)

;; defgraphql does not support queries that start with graphql comments.
(defgraphql graphql-queries "contentful/getNavTree.graphql"
  "contentful/getPages.graphql"
  "contentful/getAsset.graphql"
  "contentful/getCardList.graphql"
  "contentful/getSitemap.graphql"
  )
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
  (memoize api-call))

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
(comment (get-contentful :card-list-query {:listId "2pJ63nhY2QKbVesxgWOvq9"}))
(comment (json/write-str (:graphql ((get-in query-map [:query :asset-query]) {:assetId "34YRcoaS4WJ5ORhpMlMHRM"}))))
(comment (:variables (:graphql ((get-in query-map [:query :nav-collection-query]) {:name "Main menu"}))))
(comment (memo-clear! api-call)) ; evaluate this in REPL to clear memoize cache from api-call this allows you to update page content from contentful without restart 