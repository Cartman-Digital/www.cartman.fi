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
   [taoensso.truss :as truss :refer [have]]
   [jsonista.core :as j]))

(declare render)

;; Workaround to get grid styles compiled into the sheet.
;; Returns one of sm:grid-cols-1 sm:grid-cols-2 sm:grid-cols-3 sm:grid-cols-4
;; sm:grid-cols-5 sm:grid-cols-6
(defn get-grid-class
  [grid-count]
  (str "lg:grid-cols-" grid-count))

(defn get-embed-block-html
  [args]
  (let [entryCollection (get-in
                         (contentful/get-contentful :entry-query {:entryId (get-in args [:data :target :sys :id])})
                         [:entryCollection :items])]
    (into [:div.embed] (mapv render entryCollection))))

(defn create-entry-field-link
  [args]
  (let [entry-id (get-in args [:data :target :sys :id])
        link-label (get-in args [:content 0 :value]) 
        ;; fetch slug and title/or name
        query-result (contentful/get-contentful :entry-query {:entryId entry-id})
        {:keys [slug title name]} (get-in query-result [:entryCollection :items 0])]
    ;; render a tag use title/name as backup link-label if contentful doesnt provide
    [:a {:href (create-url slug)}
     (if (seq link-label)
       link-label
       (or title name))]))

(defn create-field
  [field]
 (let [{:keys [fieldName label fieldType]} field]
   [:div.field-wrap
    (form/label fieldName label)
    (if (= fieldType "textarea")
      [:textarea {:name fieldName :id fieldName :required ""}]
      [:input {:type fieldType :name fieldName :id fieldName :required ""}])]))

;;jsonista.core to parse schema into json
(defn render-post-json-ld [args]
  (let [schema {"@context" "https://schema.org/"
                "@type" "BlogPosting"
                "@id" (str (create-url (:slug args)) "#BlogPosting")
                :name (:title args)
                :datePublished (:publishDate args)
                :url (create-url (:slug args))
                :author {"@type" "Person"
                         ;; url uses nil as default if no args found/ else page breaks
                         "@id" (str (create-url (get-in args [:author :slug] nil)) "#Person")
                         :name (get-in args [:author :name])
                         :url (create-url (get-in args [:author :slug] nil))
                         :image {"@type" "ImageObject"
                                 "@id" (get-in args [:author :picture :url])}}
                :image {"@type" "ImageObject"
                        "@id" (get-in args [:postImage :url])
                        :url (get-in args [:postImage :url])
                        :name (get-in args [:postImage :title])}
                ;;If no keywords are provided in CMS post, default keywords used
                :keywords (or (:keywords args) ["technology",
                                                "ecommerce",
                                                "cartman.fi",
                                                "consulting",
                                                "tech blog",
                                                "technical consulting"])}]
    (j/write-value-as-string schema)))

(defn render-postCollection-json-ld []
  (let [schema {"@context" "https://schema.org/"
                "@type" "Blog"
                "@id" "https//:cartman.fi/articles.html#Blog"
                :description "List of blog posts written by Cartman digital members"
                :name "Blog"
                :url "https//:cartman.fi/articles.html"
                ;;Get 5 most relevant keywords from last 6 posts
                :keywords ["blog",
                           "technology",
                           "cartman.fi",
                           "ecommerce",
                           "ecommerce consulting"]}]
    (j/write-value-as-string schema)))

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
  [:div.title-wrap (into [:h2] (mapv richtext->html (:content m)))])

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
  (when (= (get-in args [:data :target :sys :linkType]) "Asset")
    (let [asset (:asset (contentful/get-contentful :asset-query {:assetId (get-in args [:data :target :sys :id])}))]
      (image (:url asset) (:description asset)))))

(defmethod richtext->html "embedded-entry-inline"
  [args]
 (get-embed-block-html args))

(defmethod richtext->html "entry-hyperlink"
  [args]
  (create-entry-field-link args))

(defmethod richtext->html "embedded-entry-block"
  [args]
  (get-embed-block-html args))

;;person-singleview and people-list view repeat to make styling and content changes easier
(defn person-single-view
  [args]
  (let [description (get-in args [:shortText :json])
        highlights (mapv #(render %) (get-in args [:highlightCollection :items]))
        skills (mapv #(render %) (get-in args [:skillsCollection :items]))
        tech (mapv #(render %) (get-in args [:webAppSkillsCollection :items]))
        contact-description (get-in args [:contactDescription :json])] 
    [:div.person.grid.grid-cols-1.md:grid-cols-2.sm:gap-8.items-start
     [:div.name.md:col-span-2.text-center
      [:h1.mt-4.mb-0 (:name args)]
      [:span.font-bold (str (:jobTitle args) " - ")
       [:span.office (:name (:office args))]]]
     [:div.who
      [:div.image.text-center
       [:img.inline {:alt (get-in args [:picture :title]) :src (:url (:picture args))}]]]
     [:div
      [:div.pr-2 (richtext->html description)]
      (when (not-empty highlights)
        [:div.how
         [:h2 "Work highlights"]
         (into [:ul.projects] highlights)])]
     (when (not-empty skills)
       [:div.what
        [:h2 "Skills"]
        (into [:ul.soft-skills.list-none] skills)
        (richtext->html (get-in args [:otherSkills :json]))])
     (when (not-empty tech)
       [:div.where
        [:h2 "Technologies"]
        [:div.web
         (into [:ul.web-talent.list-none] tech)]])
     (when (not-empty contact-description)
       [:div.contact-description-field
        [:h2 "Contact Description"]
        (richtext->html contact-description)])]))

(defn people-list-view
  [args]
  [:li.person-body
   [:a (if (= true (get-in args [:createPersonPage]))
         {:href (create-url (:slug args))}
         {:class "no-link-style" :href "javascript:void(0);"})
    [:h2.block.mb-0 (:name args)]
    [:span.block.font-bold.mb-2 (:jobTitle args)]
    [:div.image
     [:img {:alt (get-in args [:picture :title]) :src (:url (:picture args))}]]]
   [:div.body
    [:h2.block.mb-0 (:name args)]
    [:span.block.font-bold.mb-2 (:jobTitle args)]
    [:div.person-body (richtext->html (:json (:shortText args)))]
    [:div.contact-description (richtext->html (:json (:contactDescription args)))]]])

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
    [:section
     {:class (:cssClass args)}
     [:div {:class "intro row"}
      [:h2 (:internalName cardlist)]
      #_(richtext->html (get-in cardlist [:introduction :json]))]
     (into [:div {:class ["card-list row grid"
                          (get-grid-class (:numberOfCardColumns cardlist))]}]
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

(defmethod render "DigitalExperience"
  [args]
  (let [link (:techHomePageUrl args)
        image-url (get-in args [:logo :url])
        logo-wrap (if (empty? link) [:span.logo] [:a.logo {:href (create-url link)}])]
   [:li.digital-experience
     (merge
      logo-wrap
      (when (not-empty image-url)
        (image {:title (:name args)} image-url (:name args)))) 
    [:div
     [:span.name.block (:name args)]
     [:p (:description args)]]]))

(defmethod render "WorkHighlight"
  [args]
  [:div 
   [:span.name.block (:name args)]
   [:div.description (richtext->html (get-in args [:description :json]))]])

(defmethod render "Experience"
  [args]
  [:div.wrap
   [:div.left
    [:span.font-bold.end (if (:endYear args) (:endYear args) "Now")]
    [:span.gap-line]
    [:span.font-bold.start (:startYear args)]] 
   [:div.right
    [:span.font-bold.title (:jobTitle args)]
    [:span.font-bold.employer (:employer args)]
    ;(when (and (not (:onGoing args)) (:duration args))
    ;  [:span (str " ( " (:duration args) " months )")])
    [:div.job-description (richtext->html (get-in args [:jobDescription :json]))]]])

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
     [:div.g-recaptcha {:data-sitekey (config/get-env "CAPTCHA_PUBLIC_KEY") :data-theme "light"}]
     [:div.action.button.primary
      [:input {:type "submit" :class "submit" :value "Contact us"}]]]
    [:div {:class "success-wrap"}
     (richtext->html (get-in args [:successMessage :json]))]]))

(defmethod render "Post"
  [args]
  (let [fullbody (empty? (:shortDescription args))
        content (if (empty? (:shortDescription args)) (:content args) (:shortDescription args))]
    [(if fullbody :div.post :li.post)
     [:script {:type "application/ld+json"} (render-post-json-ld args)]
     [:div.image
      (if (:url (:postImage args))
        [:img {:alt (get-in args [:postImage :title]) :src (:url (:postImage args))}]
        [:img {:alt "working-hands-and-laptops" :src "https://images.ctfassets.net/038s6vr0kmv0/3shYr3pz9Ldd0w5qzSUYdP/d3f0af0310d1aa71a47e4130ee238040/scott-graham-5fNmWej4tAA-unsplash.jpg?h=250"}])]
     [:div.body [:a {:href (create-url (:slug args))} [:h2 (:title args)]]
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
   [:script {:type "application/ld+json"} (render-postCollection-json-ld)]
   (into [:ul.post-list] (mapv render (:items args)))])

(defmethod  render "PersonCollection"
  [args]
  [:div
   (into [:ul.people-list] (mapv render (:items args)))])

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
                        :people-by-list-query {:listId (get-in args [:sys :id])})]
    [:section.people-list-wrap 
     (into [:ul.people-list] (mapv render (get-in contentful-map [:peopleList :linkedFrom :personCollection :items])))]))

(defmethod render "Person"
  [args]
  (let [fullbody (not-empty (:highlightCollection args))]
    (if fullbody (person-single-view args) (people-list-view args))))

(defmethod render "Grid"
  [args]
  (let [title (:title args)
        cols (or (:cols args) 2)
        items (->> args :content :json :content)
        class (:cssClass args)]
    [:div.grid-wrap.block {:class class}
     [:h2 title]
     [:div.grid {:class [(str "grid-cols-" cols)]}
      (for [item items]
        [:div.item.justify-center.content-center.items-center.text-center.flex.p-8
         (richtext->html item)])]]))

(defmethod render "Carousel"
  [args]
  (let [title (:title args)
        slides (-> args :slidesCollection :items)
        class (-> title (clojure.string/replace " " "-") clojure.string/lower-case)]
    [:div 
     [:h2 {:class "embla-h2"} title]
     [:section.embla
      [:div.embla__viewport
       [:div.embla__container
        (for [slide slides]
          [:div.embla__slide
           (render slide)])]]]
          [:script {:src "https://unpkg.com/embla-carousel/embla-carousel.umd.js"}]
     [:script {:src "https://unpkg.com/embla-carousel-autoplay/embla-carousel-autoplay.umd.js"}]
     (static/get-local-js  "embla-carousel.js")]))


(defmethod render "MainBanner"
  [banner]
  (let [img (-> banner :image :url)]
    [:div.main-banner-block
     [:div.main-banner-text
      [:p.text-6xl.z-20.font-extrabold.tracking-widest {:class "text-[#FFEF5A]"}
       "ECOMMERCE IS" [:br]
       "WHAT WE DO" [:br]
       [:strong.text-white.text-xl.tracking-widest.relative.left-16
        "FUTURE-PROOF YOUR eCOMMERCE TODAY"]]]
     [:img.z-10.absolute.left-80.bottom-5.size-48 {:src img}]]))

(defmethod render "BlogLatest"
  [args]
  (let [title (:title args)
        data (contentful/get-contentful :post-collection-query {:single false})
        posts (->> data :postCollection :items (take 3))]
    [:div.blog-recent
     [:h2 title]
     [:div.flex
      (for [{:keys [postImage title slug]} posts]
        [:div.flex-1.p-8 {:class "w-full sm:w-1/4"}
         [:div.image-wrapper
          [:img {:class "w-full" :src (:url postImage)}]]
         [:div.text-wrapper
          [:a {:href slug} title]]])]
     [:div.blog-cta
      [:a {:href "/blog" :class "cta-button-blog"} "Read More"]]]))

(comment (render {:__typename "ArticleList" :sys {:id "4N25RfloTD2aq3YtrDEzLk"} :numberOfPostsShown 3}))
(comment (contentful/get-contentful :posts-by-list-query {:listId "4N25RfloTD2aq3YtrDEzLk" :limit 3}))
(comment (render-post-json-ld {:title "foobar"
                   :slug "foobar"
                   :author {:name "paavo pokkinen"
                            :title "Paavo - Head"
                            :slug "people/paavo-pokkinen"
                            :image "https://images.ctfassets.net/038s6vr0kmv0/1Dl8g5SFt7WtQLlXA17BjC/7f7ad1fb23ba59709e334f38c2ac8a07/IMG_2530.jpg"}}))
