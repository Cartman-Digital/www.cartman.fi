(ns generator.pages
  (:require
   [generator.contentful :as contentful]
   [generator.navigation :as nav]
   [generator.renderer :as renderer]
   [generator.renderer.static :as static]
   [hiccup.element :refer [link-to]]
   [hiccup.page :refer [html5 include-css include-js]]
   [taoensso.truss :as truss :refer (have)]))

(defn render-content
  [m] 
  (let [collection-items (get-in m [:contentCollection :items])]
    (into [:div.content] (if (not-empty collection-items)
                           (mapv #(renderer/render %) collection-items)
                           [(renderer/render m)]))))

(defn render-posts
  [post-collection]
  [:div.content 
   [:h1 "Blog"]
   (renderer/render (:postCollection post-collection))])

(defn render-page-head
  "Must be called from hiccup. Outputs common page html head with links to static files and metadata."
  [page-map]
  (let [id (apply str (take 5 (repeatedly #(int (rand 9)))))]
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     (when (false? (:seoIndexing page-map)) [:meta {:name "robots" :content "noindex"}])
     [:title (:title page-map)]
     [:link {:type "text/css", :href (str "/css/main.css?id=" id), :rel "stylesheet"}]
     (include-css "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.css")
     [:link {:rel "canonical" :href (nav/create-url (:slug page-map))}]
     [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
     [:script {:src "https://cdn-eu.usefathom.com/script.js" :data-site "KVDXDFWF" :defer true}]
     [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
     [:link {:rel "icon" :type "image/png" :href (static/get-asset-url "favicon.png")}]
     [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;700&family=Ubuntu:wght@300;700&display=swap"}]]))

(defn render-page-footer
  "Must be called from hiccup. Outputs common page footer in a vector."
  []
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
              [:svg {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 1200 1227"}
               [:path {:fill "rgb(255,255,255)" :d "M714.163 519.284L1160.89 0H1055.03L667.137 450.887L357.328 0H0L468.492 681.821L0 1226.37H105.866L515.491 750.218L842.672 1226.37H1200L714.137 519.284H714.163ZM569.165 687.828L521.697 619.934L144.011 79.6944H306.615L611.412 515.685L658.88 583.579L1055.08 1150.3H892.476L569.165 687.854V687.828Z"}]]
              [:span {:class "sr-only"} "Twitter"]])
    
    (link-to "https://www.itewiki.fi/cartman-digital"
             [:span.icon
                 [:svg {:xmlns "http://www.w3.org/2000/svg"  :width "24" :height "24" :viewBox "0 0 177.7 177.7" :x "0px" :y "0px" :style "enable-background:new 0 0 177.7 177.7;" :xml:space "preserve"}
                  [:g 
                   [:path {:fill "rgb(255,255,255)"  :d "M84.3,129l-40-40.3L0.2,44.2L0,133.5l39.8,39.6L84.3,129L84.3,129z M93.4,48.7l40,40.3l44.1,44.4l0.2-0.2V44.2L137.9,4.6L93.4,48.7z"}]
                   [:polygon  {:fill "rgb(255,255,255)"   :points "129,93.4 88.6,133.4 44.2,177.5 133.5,177.7 173.1,137.9"}]
                   [:polygon  {:fill "rgb(255,255,255)"  :points "48.7,84.3 89,44.3 133.5,0.2 44.2,0 4.6,39.8"}]]]
                 [:span {:class "sr-only"} "Itewiki"]])]])

(defn render-page
  [page body-class]
  (html5
   (render-page-head {:slug (:slug page)
                      :title (:title page)
                      :seoIndexing (:seoIndexing page)})
   [:body {:class (if (= (:slug page) "/")
                    (str "front-page" " " body-class)
                    (str (:slug page) " " body-class))}
    (nav/render-main-menu)
    (render-content page)
    (render-page-footer)
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.js")]))

(defn render-post-list-page
  []
  (html5
   (render-page-head {:seoIndexing true
                      :title "Articles"
                      :slug "articles"})
   [:body {:class "postlist"}
    (nav/render-main-menu)
    (render-posts (contentful/get-contentful :post-collection-query {:single false}))
    (render-page-footer)
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.js")]))

(defn get-body-class
  [default bool-narrow]
  (str default " " (when bool-narrow "narrow")))

(defn get-static-pages 
  [m]
  (let [pages-data (contentful/get-contentful :page-collection-query)
        pages (have vector? (get-in pages-data [:pageCollection :items]))] 
    (into m (mapv #(vector
            (if (not= (:slug %) "/") (str "/" (:slug %) ".html") (str "/index.html"))
            (fn [context] (render-page % (get-body-class "static-page" (:useNarrowLayout %))))) pages))))

(defn get-post-pages
  "Returns map of filename (eg. index.html) -> html"
  [m]
  (let [posts-data (contentful/get-contentful :post-collection-query )
        posts (have vector? (get-in posts-data [:postCollection :items]))]
   (into m (mapv #(vector
            (str "/" (:slug %) ".html")
            (fn [context] (render-page % "post-page"))) posts))))

(defn get-person-pages
  "Returns map of filename (eg. index.html) -> html"
  [m]
  (let [people-data (contentful/get-contentful :person-collection-query {:list false :where {:createPersonPage true}})
        people (have vector? (get-in people-data [:personCollection :items]))]
    (into m (mapv #(vector
                    (str "/" (:slug %) ".html")
                    (fn [context] (render-page % "person-page"))) people))))

(defn get-pages
  "Creates a map of page slug -> render calls."
  []
  (-> {"/articles.html" (fn [context] (render-post-list-page))}
      get-static-pages
      get-post-pages
      get-person-pages))

(comment (:postCollection (contentful/get-contentful :post-collection-query {:type ["news" "article"]})))
(comment (get-pages)
         (get-post-pages {}))

(comment
  (require '[stasis.core :as stasis])

  (def pages
    (->> (contentful/get-contentful :page-collection-query) :pageCollection :items
         (reduce #(assoc %1 (:slug %2) %2) {})))

  (let [page (get pages "/")]
    (stasis/export-page "/" (render-page page "") "./build" {})))

