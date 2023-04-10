(ns generator.contentful
  (:require
   [taoensso.truss :as truss :refer (have)]
   [graphql-builder.parser :refer [defgraphql]] 
   [graphql-builder.core :as graphql-core]
   [clj-http.client :as client] 
   [muuntaja.core :as m]))
   

(def contentful-space "038s6vr0kmv0")
(def contentful-environment "master")
(def contentful-url (str "https://graphql.contentful.com/content/v1/spaces/" 
                         contentful-space 
                         "/environments/" 
                         contentful-environment))
(def headers {:Authorization "Bearer QJSIZZ8-mkUZPBySwQTtVwfirskzh2ZnplerHDo6xpE"})

(declare graphql-queries)
(defgraphql graphql-queries "contentful/getNavTree.graphql" "contentful/getPages.graphql")
(def query-map (graphql-core/query-map graphql-queries))

(comment (get-contentful :nav-collection-query {:name "Main menu"}))

(def ^:private dispatch
  (memoize
   (fn [query]
     (let [graphql (have map? (:graphql query))]
       (->> (client/get contentful-url {:accept :json
                                        :debug false
                                        :query-params {:query (:query graphql)
                                                       :variables (:variables graphql)}
                                        :headers headers 
                                        :throw-entire-message? true})
            (m/decode-response-body)
            (:data))))))

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
