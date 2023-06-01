(ns generator.pages
  (:require
   [generator.contentful :as contentful]
   [generator.navigation :as nav]
   [generator.renderer :as renderer]
   [hiccup.element :refer [link-to]]
   [hiccup.page :refer [html5 include-css include-js]]
   [taoensso.truss :as truss :refer (have)]))

(def assets-version (apply str "v/" (repeatedly 5 #(rand-int 9))))

(defn render-content
  [content] 
  (let [collection-items (get-in content [:contentCollection :items])] 
    (into [:div.content] (mapv #(renderer/render %) collection-items))))

(defn render-page
  [page]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title (get page :title)]
    (include-css (nav/prepend-base-url (str "assets/" assets-version "/main.css")))
    (include-css "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.css")
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:script {:src "https://cdn-eu.usefathom.com/script.js" :data-site "KVDXDFWF" :defer true}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
    [:link {:rel "icon" :type "image/png" :href (nav/prepend-base-url (str "assets/" assets-version "/favicon.png"))}]
    [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;700&family=Ubuntu:wght@300;700&display=swap"}]]
   [:body {:class (:slug page)}
    (nav/render-main-menu)
    (render-content page)
    [:footer {:class "footer"}
     [:span "&copy 2023 Cartman Digital Oy"]
     [:div.social-media 
      (link-to "https://www.linkedin.com/company/cartmandigital/"
               [:span.icon 
                [:svg {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24"}
                 [:path {:fill "currentColor" :d "M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.79-1.75-1.764s.784-1.764 1.75-1.764 1.75.79 1.75 1.764-.783 1.764-1.75 1.764zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z"}]]
                [:span {:class "sr-only"} "LinkedIn"]])
      (link-to "https://twitter.com/CartmanDigital"
               [:span.icon
                [:svg {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24"}
                 [:path {:fill "currentColor" :d "M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-.139 9.237c.209 4.617-3.234 9.765-9.33 9.765-1.854 0-3.579-.543-5.032-1.475 1.742.205 3.48-.278 4.86-1.359-1.437-.027-2.649-.976-3.066-2.28.515.098 1.021.069 1.482-.056-1.579-.317-2.668-1.739-2.633-3.26.442.246.949.394 1.486.411-1.461-.977-1.875-2.907-1.016-4.383 1.619 1.986 4.038 3.293 6.766 3.43-.479-2.053 1.08-4.03 3.199-4.03.943 0 1.797.398 2.395 1.037.748-.147 1.451-.42 2.086-.796-.246.767-.766 1.41-1.443 1.816.664-.08 1.297-.256 1.885-.517-.439.656-.996 1.234-1.639 1.697z"}]]
                [:span {:class "sr-only"} "Twitter"]])]]
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.js")]))

(defn get-pages
  "Returns map of filename (eg. index.html) -> html" 
  []
  (let [pages-data (contentful/get-contentful :page-collection-query)
        pages (have vector? (get-in pages-data [:pageCollection :items]))]
    (into {} (mapv #(vector
                     (if (not= (:slug %) "/") (str "/" (:slug %) ".html") (str "/index.html"))
                     (fn [context] (render-page %))) pages))))
