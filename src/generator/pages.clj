(ns generator.pages
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful] 
            [generator.navigation :as nav]
            [hiccup.page :use [html5 include-css include-js]]
            [hiccup.element :refer (link-to image)]))

(defn render-content [page]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    ]
   [:body
    (nav/render-main-menu)
    [:div.content page]
    [:div.footer
     [:span "&copy 2018 Nick George"]]]))

(defn get-pages
  "Returns map of filename (eg. index.html) -> html"
  []
  (let [pages-data (contentful/get-contentful :page-collection-query)
        pages (have vector? (get-in pages-data [:pageCollection :items]))]
    (mapv #(vector
           (if (not= (:slug %) "/") (str (:slug %) ".html") (str "index.html"))
           (render-content %)) pages)))


(comment
  (pprint (second (first (get-pages))))
  )