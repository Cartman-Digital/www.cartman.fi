(ns generator.pages
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful] 
            [generator.navigation :as nav] 
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer (link-to image)]))

(defn render-content [content]
  (into [] [:p "some content"]))


(defn render-page [page]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title (get page :title)]
    (include-css "/assets/main.css")
    (include-css "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.css")
    ]
   [:body {:class "bg-black text-white"}
    (nav/render-main-menu)
    [:div {:class ""} (render-content page)]
    [:div.footer
     [:span "&copy 2023 Cartman Digital Oy"]]
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.js")]))

(defn get-pages
  "Returns map of filename (eg. index.html) -> html"
  []
  (let [pages-data (contentful/get-contentful :page-collection-query)
        pages (have vector? (get-in pages-data [:pageCollection :items]))]
    (into {} (mapv #(vector
           (if (not= (:slug %) "/") (str "/" (:slug %) ".html") (str "/index.html"))
           (render-page %)) pages))))

(comment
  (println (get-pages)))