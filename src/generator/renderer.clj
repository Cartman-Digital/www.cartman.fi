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
   [hiccup.element :refer [image]]
   [hiccup.form :as form]
   [hiccup.page :as page]
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

(defn newline->br [s]
  (clojure.string/replace s #"\r\n|\n|\r" "<br />\n"))

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
  (into [:a {:href (static/prepend-base-url (get-in m [:data :uri]))}]
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
          (newline->br (:value m))
          (:marks m)))

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
  (println args)
  [:li.post 
   [:h2 (:title args)]
   [:p [:span.author (get-in args [:author :name])]]
   [:p [:span.published (:publishDate args)]]
   [:div.post-body (richtext->html (get-in args [:content :json]))]])

(defmethod render "PostCollection"
  [args]
  [:div 
   (into [:ul.post-list] (mapv render (:items args)))])

(comment (render {:__typename "PostCollection",
                  :items
                  [{:__typename "Post",
                    :content
                    {:json
                     {:nodeType "document",
                      :content
                      [{:nodeType "paragraph",
                        :content
                        [{:nodeType "text",
                          :value "So happy to announce that Cartman has been reborn and is now Cartman Digital. Woop woop!",
                          :marks [],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph",
                        :content
                        [{:nodeType "text",
                          :value
                          "The new Cartman Digital has a unique team with vast experience in both business and technical development. We have done it all, from B2B to B2C to D2C, and we have done it together.",
                          :marks [],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph",
                        :content [{:nodeType "text", :value "Meet our amazing team:", :marks [], :data {}}],
                        :data {}}
                       {:nodeType "unordered-list",
                        :content
                        [{:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content [{:nodeType "text", :value "Janne Sävy, Commercial guy", :marks [], :data {}}],
                            :data {}}],
                          :data {}}
                         {:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content
                            [{:nodeType "text", :value "", :marks [], :data {}}
                             {:nodeType "embedded-entry-inline",
                              :content [],
                              :data {:target {:sys {:type "Link", :id "6VQ6AYyBkWg22mN10i5DrA", :linkType "Entry"}}}}
                             {:nodeType "text", :value ", Technical guy", :marks [], :data {}}],
                            :data {}}],
                          :data {}}
                         {:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content [{:nodeType "text", :value "Tommi Martin, Development guy", :marks [], :data {}}],
                            :data {}}],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph",
                        :content
                        [{:nodeType "text",
                          :value
                          "Our team is small, but with a modern tech stack like ours, you don't need an army of developers to build what your business needs or to maintain it. Pay for business value, not for excessive meetings, project \"extras\" just hanging around, company overhead, constant tech stack updates or anything else not needed. For things we don't do, we have trusted partners.",
                          :marks [],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph", :content [{:nodeType "text", :value "What we do:", :marks [], :data {}}], :data {}}
                       {:nodeType "unordered-list",
                        :content
                        [{:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content [{:nodeType "text", :value "Commerce solutions", :marks [], :data {}}],
                            :data {}}],
                          :data {}}
                         {:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content [{:nodeType "text", :value "Software development", :marks [], :data {}}],
                            :data {}}],
                          :data {}}
                         {:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content [{:nodeType "text", :value "Integrations", :marks [], :data {}}],
                            :data {}}],
                          :data {}}
                         {:nodeType "list-item",
                          :content
                          [{:nodeType "paragraph",
                            :content [{:nodeType "text", :value "Business and technical consulting", :marks [], :data {}}],
                            :data {}}],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph",
                        :content
                        [{:nodeType "text",
                          :value
                          "We are WYSIWYG: What you see is what you get. We don't have a sales team or pre-sales team, only our A team, from start to finish. You will get real value starting from the first meeting. Get in touch to test us!",
                          :marks [],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph",
                        :content
                        [{:nodeType "text",
                          :value
                          "Customer experience is key for us and our customers. We enable you to build your architecture based on your business needs and make it future-proof. No more forcing solutions where they don't naturally fit. The age of monoliths is over.",
                          :marks [],
                          :data {}}],
                        :data {}}
                       {:nodeType "paragraph",
                        :content [{:nodeType "text", :value "Work smarter, work with Cartman!", :marks [], :data {}}],
                        :data {}}],
                      :data {}}},
                    :type ["news"],
                    :publishDate "2023-04-05T00:00:00.000+03:00",
                    :title "Cartman Digital: The Game-Changing Team for Your Business",
                    :author {:name "Paavo Pokkinen"}}]}))
