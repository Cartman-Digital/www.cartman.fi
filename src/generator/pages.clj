(ns generator.pages
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]
            [generator.pages :as pages]))

(defn render-content [page]
  (str "<h1>test</h1>"))

(defn get-pages
  "Returns map of filename (eg. index.html) -> html"
  []
  (let [pages-data (contentful/get-contentful :page-collection-query)
        pages (have vector? (get-in pages-data [:pageCollection :items]))]
    (mapv #(vector
           (if (not= (:slug %) "/") (str (:slug %) ".html") (str "index.html"))
           (render-content %)) pages)))


(comment
  (pprint (get-pages))
  )