;; Heavily based on https://github.com/john-shaffer/clj-contentful 
;; Installed into project due to being unable to resolve dependencies
;; From project.clj vs deps.edn configuration
;; Strips out all communication parts from the original code. Rely on existing information.
;; Updates method to return hiccup syntax
(ns generator.renderer
  (:require
   [generator.config :as config]
   [generator.contentful :as contentful]
   [generator.navigation :refer [create-url]]
   [generator.renderer.static :as static]
   [generator.renderer.util :as renderer.util]
   [hiccup.element :refer [image]]
   [hiccup.form :as form]
   [hiccup.util :as util]
   [taoensso.truss :as truss :refer [have]]))

(declare render)

;; Workaround to get grid styles compiled into the sheet.
;; Returns one of sm:grid-cols-1 sm:grid-cols-2 sm:grid-cols-3 sm:grid-cols-4
;; sm:grid-cols-5 sm:grid-cols-6
(defn get-grid-class
  [grid-count]
  (str "sm:grid-cols-" grid-count))

(defn get-embed-block-html
  [args]
  (let [entryCollection (get-in (contentful/get-contentful :entry-query {:entryId (get-in args [:data :target :sys :id])}) [:entryCollection :items])]
    (into [:div.embed] (mapv render entryCollection))))

(defn create-field
  [field]
 (let [{:keys [fieldName label fieldType]} field]
   [:div.field-wrap
    (form/label fieldName label)
    (if (= fieldType "textarea")
      [:textarea {:name fieldName :id fieldName :required ""}]
      [:input {:type fieldType :name fieldName :id fieldName :required ""}])]))

;; https://github.com/contentful/rich-text/blob/master/packages/rich-text-types/src/marks.ts
;; Used automatically by richtext->html
(defmulti apply-text-mark
  (fn [mark content]
    mark))

(defmethod apply-text-mark "bold"
  [mark content]
  [:strong content])

(defmethod apply-text-mark "code"
  [mark content]
  [:code content])

(defmethod apply-text-mark "italic"
  [mark content]
  [:em content])

(defmethod apply-text-mark "underline"
  [mark content]
  [:u content])

;; Entry-point expects richtext mapv, first level must contain nodeType key.
(defmulti richtext->html :nodeType)

(defmethod richtext->html :default
  [m]
  m)

(defmethod richtext->html "blockquote"
  [m]
  (into [:blockquote] (mapv richtext->html (:content m))))

(defmethod richtext->html "document"
  [m]
  (into [:div.doc] (mapv richtext->html (:content m))))

(defmethod richtext->html "heading-1"
  [m]
  (into [:h1] (mapv richtext->html (:content m))))

(defmethod richtext->html "heading-2"
  [m]
  (into [:h2] (mapv richtext->html (:content m))))

(defmethod richtext->html "heading-3"
  [m]
  (into [:h3] (mapv richtext->html (:content m))))

(defmethod richtext->html "heading-4"
  [m]
  (into [:h5] (mapv richtext->html (:content m))))

(defmethod richtext->html "heading-5"
  [m]
  (into [:h5] (mapv richtext->html (:content m))))

(defmethod richtext->html "heading-6"
  [m]
  (into [:h6] (mapv richtext->html (:content m))))

(defmethod richtext->html "hr"
  [m]
  [:hr])

(defmethod richtext->html "hyperlink"
  [m]
  (into [:a {:href (create-url (get-in m [:data :uri]))}]
   (mapv richtext->html (:content m))))

(defmethod richtext->html "list-item"
  [m]
  (into [:li] (mapv richtext->html (:content m))))

(defmethod richtext->html "ordered-list"
  [m]
  (into [:ol] (mapv richtext->html (:content m))))

(defmethod richtext->html "paragraph"
  [m]
  (into [:p] (mapv richtext->html (:content m))))

(defmethod richtext->html "text"
  [m]
  (reduce #(apply-text-mark (:type %2) %)
          (util/escape-html (:value m))
          (:marks m)))

(defmethod richtext->html "table"
  [m]
  (into [:table] (mapv richtext->html (:content m))))

(defmethod richtext->html "table-row"
  [m]
  (into [:tr] (mapv richtext->html (:content m))))

;; Todo: requires colspan support
(defmethod richtext->html "table-header-cell"
  [m]
  (into [:th] (mapv richtext->html (:content m))))

(defmethod richtext->html "table-cell"
  [m]
  (into [:td] (mapv richtext->html (:content m))))

(defmethod richtext->html "unordered-list"
  [m]
  (into [:ul] (mapv richtext->html (:content m))))

(defmethod richtext->html "embedded-asset-block"
  [args]
  (if (= (get-in args [:data :target :sys :linkType]) "Asset")
    (let [asset (:asset (contentful/get-contentful :asset-query {:assetId (get-in args [:data :target :sys :id])}))]
      (image (:url asset) (:description asset)))))

(defmethod richtext->html "embedded-entry-inline"
  [args]
  (get-embed-block-html args))

(defmethod richtext->html "embedded-entry-block"
  [args]
  (get-embed-block-html args))

(defmulti render :__typename)

(defmethod render :default
 [args]
 args)

(defmethod render "CtaBanner"
  [args] 
  [:div {:class "hero-banner" :style (str "background-image: url(" (get-in args [:banner :url]) ");")}
   [:div {:class "banner-content-wrap"}
    [:div {:class "banner-text"}
     (richtext->html (get-in args [:bannerText :json])) 
     (if (not (empty? (:ctaButtonText args)))
     [:a {:href (get args :ctaUrl) :class "cta button action primary"} (get args :ctaButtonText)])]]])

(defmethod render "ContentBlock"
  [args] 
  (richtext->html (get-in args [:content :json])))

(defmethod render "SideBySide"
  [args]
  [:section
   [:div {:class "row"}
   [:div {:class "side-by-side left"}
    (richtext->html (get-in args [:leftColumn :json]))]
   [:div {:class "side-by-side right"}
    (richtext->html (get-in args [:rightColumn :json]))]]])

(defmethod render "CardList"
 [args]
 (let [cardlist (:cardList (contentful/get-contentful :card-list-query {:listId (get-in args [:sys :id])})) 
       cardCollection (get-in cardlist [:cardsCollection :items])]
   [:section (into [:div {:class (str "card-list row grid " (get-grid-class (:numberOfCardColumns cardlist)))} 
          [:div {:class "intro"} 
           (richtext->html (get-in cardlist [:introduction :json]))]]
         (mapv #(render %) cardCollection))]))

(defmethod render "Nav"
 [args]
 (let [menu-data (contentful/get-contentful :nav-collection-query {:name (:name args)})
       nav-item-collection (have map? (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
   [:div {:class "sub-nav"}
    [:ul {:class "grid sm:grid-cols-2 lg:grid-cols-4"}
     (for [item (:items nav-item-collection)]
       [:li {:class (str "nav-item " (:additionalCssClasses item))}
        [:a {:href (create-url (:slug item))} (get item :title)]])]]))

(defmethod render "Form" 
 [args]
 (let [id (random-uuid)
       form-fields (get-in args [:fieldsCollection :items])]
   [:div.form-wrap
    [:script {:type "text/javascript", :src "https://www.google.com/recaptcha/api.js" :defer "" :async ""}] 
    [:form {:id id :action (str (config/get-env "BASE_URL") (:actionUrl args)) :method "POST" :onsubmit "return submitForm(this);"}
     [:div.bar-loader 
      [:div]
      [:div]
      [:div]]
     [:div.message-container
      [:p.message ""]]
     (static/get-local-js "async-form.js")
     (for [field form-fields]
       (create-field field))
     [:div.g-recaptcha {:data-sitekey (config/get-env "CAPTCHA_PUBLIC_KEY") :data-theme "dark"}]
     [:div.action.button.primary
      [:input {:type "submit" :class "submit" :value "Contact us"}]]]
    [:div {:class "success-wrap"}
     (richtext->html (get-in args [:successMessage :json]))]]))

(defmethod render "Post"
  [args]
  (let [fullbody (empty? (:shortDescription args))
        content (if (empty? (:shortDescription args)) (:content args) (:shortDescription args))]
    [(if fullbody :div.post :li.post)
     [:div.image
      (if (:url (:postImage args))
        [:img {:alt (get-in args [:postImage :title]) :src (:url (:postImage args))}]
        [:img {:alt "working-hands-and-laptops" :src "https://images.ctfassets.net/038s6vr0kmv0/3shYr3pz9Ldd0w5qzSUYdP/d3f0af0310d1aa71a47e4130ee238040/scott-graham-5fNmWej4tAA-unsplash.jpg?h=250"}])]
     [:div.body [:a {:href (create-url (:slug args))}[:h2 (:title args)]]
      [:div.author
       [:span.name (get-in args [:author :name])]
       [:span.published (renderer.util/iso-to-relative (:publishDate args))]]
      (into [:div.types] (mapv #(vector :span {:class (str "type " %)} %) (:type args)))
      [:div.post-body (richtext->html (:json content))]
      (if (not fullbody)
        [:a {:class "button action primary" :href (create-url (:slug args))} "Read more"]
        nil)]]))


(defmethod render "PostCollection"
  [args]
  [:div
   (into [:ul.post-list] (mapv render (:items args)))])


 
(defmethod  render "PersonCollection" 
 [args]
  [:div
   (into [:ul.people-list] (mapv render (:items args)))]
 )

(defmethod render "ArticleList"
  [args]
  (let [contentful-map (contentful/get-contentful
                        :posts-by-list-query {:listId (get-in args [:sys :id])
                                              :limit (:numberOfPostsShown args)})]
    [:section.post-list-wrap
     [:div.intro
      [:h2 (get-in contentful-map [:articleList :websiteTitle])]]
     (into [:ul.post-list] (mapv render (get-in contentful-map [:articleList :linkedFrom :postCollection :items])))]))


(defmethod render "PeopleList"
  [args]
  (let [contentful-map (contentful/get-contentful
                        :people-by-list-query{:listId (get-in args [:sys :id])})]
    [:section.people-list-wrap
     [:div.intro
      [:h2 "Personel-Our Amazing Team :D"]]
     (into [:ul.people-list] (mapv render (get-in contentful-map [:peopleList :linkedFrom :personCollection :items])))
     ]
    ))


; Todo implement two person logics: embedded and person page.
;; WIP: people list page does not get short version of info 
(defmethod render "Person"
  [args]
  (println (empty? (:shortText args)))
  (let [fullbody (empty? (:shortText args))
        content (if (empty? (:shortText args)) (:description args) (:shortText args))]
    [(if fullbody :div.person :li.person-body)
     [:div.image
      [:img {:alt (get-in args [:picture :title]) :src (:url (:picture args))}]]
     [:div.body [:a {:href (create-url (:slug args))} [:h3 (:name args)]]
      (into [:div.types] (mapv #(vector :span {:class (str "type " %)} %) (:type args)))
      [:div.person-body (richtext->html (:json content))]
      [:div.tech-logos [:h3 "Logos go here"]]]]))

(comment (render {:__typename "ArticleList" :sys {:id "4N25RfloTD2aq3YtrDEzLk"} :numberOfPostsShown 3}))
(comment (contentful/get-contentful
          :posts-by-list-query {:listId "4N25RfloTD2aq3YtrDEzLk"
                                :limit 3}))
