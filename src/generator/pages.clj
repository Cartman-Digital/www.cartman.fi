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
     (static/get-local-css "main.css")
     [:link {:type "text/css", :href (str "/css/main.css?id=" id), :rel "stylesheet"}]
     (include-css "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.css")
     [:link {:rel "canonical" :href (nav/create-url (:slug page-map))}]
     [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
     [:script {:src "https://cdn-eu.usefathom.com/script.js" :data-site "KVDXDFWF" :defer true}]
     [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
     [:link {:rel "icon" :type "image/png" :href (static/get-asset-url "favicon.png")}]
     [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Akshar:wght@300..700&family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&family=Ubuntu:ital,wght@0,300;0,400;0,500;0,700;1,300;1,400;1,500;1,700&display=swap"}]]))

(defn menu-footer []
  (let [menu-data (contentful/get-contentful :nav-collection-query {:name nav/top-nav})
        nav-item-collection (have map?
                                  (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
    [:ul.menu
     (for [item (:items nav-item-collection)]
       [:li
        [:a {:href (:slug item)} (get item :title)]])]))

(def linkedin-icon
  [:span
   [:svg {:xmlns "http://www.w3.org/2000/svg" :fill "none" :width "46" :height "46" :viewBox "0 0 46 46"}
    [:g :clip-path= "url(#clip0_2_635)"
     [:path {:fill "#2A2A2A" :d "M36.8593 2.83521H2.82647C1.26704 2.83521 0 4.11997 0 5.69712V39.6679C0 41.2451 1.26704 42.5299 2.82647 42.5299H36.8593C38.4188 42.5299 39.6947 41.2451 39.6947 39.6679V5.69712C39.6947 4.11997 38.4188 2.83521 36.8593 2.83521ZM11.997 36.8592H6.11369V17.9156H12.0059V36.8592H11.997ZM9.05534 15.3284C7.16808 15.3284 5.64408 13.7955 5.64408 11.9171C5.64408 10.0387 7.16808 8.50587 9.05534 8.50587C10.9338 8.50587 12.4666 10.0387 12.4666 11.9171C12.4666 13.8044 10.9426 15.3284 9.05534 15.3284ZM34.0506 36.8592H28.1673V27.6444C28.1673 25.447 28.123 22.6205 25.1104 22.6205C22.0447 22.6205 21.5751 25.0128 21.5751 27.4849V36.8592H15.6918V17.9156H21.3359V20.5029H21.4156C22.2042 19.0143 24.1269 17.446 26.9888 17.446C32.943 17.446 34.0506 21.3712 34.0506 26.4748V36.8592Z"}]]
    [:defs [:clipPath :id "clip0_2_635"]
     [:rect :width "39.6947" :height "45.3653" :fill "white"]]]])

(def itewiki-icon
  [:span
   [:svg {:xmlns "http://www.w3.org/2000/svg" :fill "none" :width "46" :height "46" :viewBox "0 0 46 46"}
    [:path {:d "M31.1613 0H9.4839L0.270996 9.75484L10.2968 19.7806L31.1613 0Z"
            :fill "#2A2A2A"}]
    [:path {:d "M42 31.1614L41.4491 9.81976L31.9742 0.812988L21.4065 11.1098L42 31.1614Z"
            :fill "#2A2A2A"}]
    [:path {:d "M10.8386 42.542L32.1802 41.9911L41.187 32.2452L30.8902 21.9484L10.8386 42.542Z"
            :fill "#2A2A2A"}]
    [:path {:d "M0 11.1097L0 32.7871L9.75484 42L19.7806 31.7032L0 11.1097Z"
            :fill "#2A2A2A"}]]])

(defn render-page-footer
  "Must be called from hiccup. Outputs common page footer in a vector."
  []
  (let [cta-button-text "CONTACT US"
        cta-button-url "https://www.cartman.fi/contact.html"]
    [:div.place-items-center.grid.grid-cols-1
     [:div.foot.grid.sm:justify-items-center.grid-cols-1.sm:grid-cols-3 {:class "footer"}
      (menu-footer)
      [:hr]
      [:ul
       [:li "Social media"]
       [:li
        [:a
         {:href "https://fi.linkedin.com/company/cartmandigital"}
         linkedin-icon
         "LinkedIn"]] 
       [:li
        [:a
         {:href "https://www.itewiki.fi/cartman-digital"}
         itewiki-icon
         "Itewiki"]]]
      [:hr]
      [:ul
       [:li "Company"]
       [:li "Cartman Digital Oy"]
       [:li "Tietotie 2, 90460 OULU"]
       [:li "Phone: +358 40 088 0369"]
       [:li "name.lastname@cartman.fi"]
       [:li
        [:a {:href cta-button-url :class "cta-button-footer"} cta-button-text]]]  
      [:hr]
      [:div {:class "footer-centering-div"}]
      [:div.text-md.pb-5 "&copy 2024 Cartman Digital Oy"]]]))

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
