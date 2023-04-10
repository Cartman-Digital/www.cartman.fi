(ns generator.contentful
  (:require
   [graphql-builder.parser :refer[defgraphql]] 
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
(defgraphql graphql-queries "contentful/getNavTree.graphql")
(def query-map (graphql-core/query-map graphql-queries))

(comment (pprint (get-nav-tree {:name "Main menu"})))


(defn dispatch [query]
  (let [graphql (:graphql query)] 
    (->> (client/get contentful-url {:accept :json 
                                     :debug false
                                       :query-params {:query (:query graphql) 
                                                      :variables (:variables graphql)} 
                                       :headers headers 
                                       :throw-entire-message? false})
           (m/decode-response-body)
           (:data))))

(defn get-nav-tree [filter]
  (let [query-fn (get-in query-map [:query :nav-collection-query])
        query (query-fn filter)]
    (dispatch query)))