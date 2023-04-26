(ns generator.pages
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]
            [generator.navigation :as nav] 
            [generator.renderer :as renderer] 
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer (link-to image)]))

(defn render-content
  [content] 
  (let [collection-items (get-in content [:contentCollection :items])] 
    (into [:div.content] (mapv #(renderer/render %) collection-items))))

(defn example-content
  "Development tool: prints out significant example content"
  []
  [:div {:class "content-wrapper"}
   [:div {:class "row-wrap"}
    [:div
     [:h1 "H1 heading"]
     [:h2 "H2 heading"]
     [:h3 "H3 heading"]
     [:h4 "H4 heading"]
     [:h5 "H5 heading"]
     [:h6 "H6 heading"]
     [:br]
     [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tempor, dui ac semper pretium, risus nisl finibus nibh, eu finibus diam libero varius lectus. Mauris dictum dignissim est, ut scelerisque orci iaculis a. Aliquam pharetra velit in lectus lacinia egestas. Curabitur vel diam tellus. Sed pellentesque risus quis porttitor volutpat. Donec eu feugiat metus. Morbi eget odio quis leo lobortis imperdiet sed dignissim nisl."]
     [:br]
     [:div
      [:span {:class "italic"} "Italic example"]]
     [:div
      [:span {:class "font-bold"} "bold example"]]
     [:div
      [:a {:href "#" :class ""} "link example"]]
     [:br]
     [:ul {:class "list-disc"}
      [:li "unordered list element 1"]
      [:li "unordered list element 2"]]
     [:br]
     [:ol {:class "list-decimal"}
      [:li "Ordered element 1"]
      [:li "Ordered element 2"]]
     [:br]
     [:ul {:class "no-list-style"}
      [:li "List element without list styles 1"]
      [:li "Element without list styles 2"]]
     [:br]
     [:div {:class "form-container"}
      [:form {:method "POST" :action-url "/post-results" :class "p-1 max-w-screen-xl m-auto"}
       [:h3 "Contact form example"]
       [:label {:for "first-input"} "Firstname"]
       [:input {:type "text" :id "first-input" :placeholder "Firstname" :name "first-input"}]
       [:label {:for "last-input"} "lastname"]
       [:input {:type "text" :id "last-input" :placeholder "Lastname" :name "last-input"}]
       [:label {:for "email"} "Email"]
       [:input {:type "email" :id "email" :placeholder "emanuel.example@test.com" :name "email"}]
       [:label {:for "phone"} "phone"]
       [:input {:type "text" :id "phone " :name "phone"}]
       [:label {:for "text-area-input"} "How can we be of assistance?"]
       [:input {:type "textarea" :name "text-area-input"}]
       [:br]
       [:h6 "How would you like to be contacted"]
       [:input {:type "radio" :id "rad-1" :name "rad-1" :value "choice-1"}]
       [:label {:for "rad-1"} "Email"]
       [:br]
       [:input {:type "radio" :id "rad-2" :name "rad-1" :value "choice-2"}]
       [:label {:for "rad-2"} "Phone"]
       [:br]
       [:input {:type "checkbox" :id "checkbox-example" :name "checkbox-example"}]
       [:label {:for "checkbox-example"} "I have read the privacy policy and agree to it's terms"]
       [:br]
       [:input {:type "submit" :class "button action primary"}]]]]]
   [:div {:class "hero-banner"}
    [:div {:class "banner-content-wrap"}
     [:div {:class "banner-text"}
      [:h1 {:class "primary-title"} "Banner-title"]
      [:p {:class "banner text content"} "Banner text content. Can contain paragraph that is anywhere from a few words or more to something much much larger. For example multiple paragraphs like this."]
      [:button {:class "cta button action primary"} "Banner-CTA button"]
      [:button {:class "cta button action"} "Banner secondary button"]]]]
   [:div {:class "row-wrap"}
    [:div {:class "side-by-side left"}
     [:img {:src "/assets/images/cartman_digital_logo.png" :alt "Cartman Digital"}]]
    [:div {:class "side-by-side right"}
     [:h2 "Side By Side title"]
     [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tempor, dui ac semper pretium, risus nisl finibus nibh, eu finibus diam libero varius lectus. Mauris dictum dignissim est, ut scelerisque orci iaculis a. Aliquam pharetra velit in lectus lacinia egestas. Curabitur vel diam tellus. Sed pellentesque risus quis porttitor volutpat. Donec eu feugiat metus. Morbi eget odio quis leo lobortis imperdiet sed dignissim nisl."]]]])

(defn render-page [page]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title (get page :title)]
    (include-css "/assets/main.css")
    (include-css "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.css")
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
    [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&family=Ubuntu:wght@700&display=swap"}]]
   [:body
    (nav/render-main-menu)
    (render-content page)
    [:footer {:class "footer"}
     [:span "&copy 2023 Cartman Digital Oy"]
     [:div.social-media 
      (link-to "https://linkedin.com" 
               [:span.icon 
                [:svg {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24"}
                 [:path {:fill "currentColor" :d "M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.79-1.75-1.764s.784-1.764 1.75-1.764 1.75.79 1.75 1.764-.783 1.764-1.75 1.764zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z"}]]
                [:span {:class "sr-only"} "LinkedIn"]])
      (link-to "https://twitter.com"
               [:span.icon
                [:svg {:xmlns "http://www.w3.org/2000/svg" :width "24" :height "24" :viewBox "0 0 24 24"}
                 [:path {:fill "currentColor" :d "M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-.139 9.237c.209 4.617-3.234 9.765-9.33 9.765-1.854 0-3.579-.543-5.032-1.475 1.742.205 3.48-.278 4.86-1.359-1.437-.027-2.649-.976-3.066-2.28.515.098 1.021.069 1.482-.056-1.579-.317-2.668-1.739-2.633-3.26.442.246.949.394 1.486.411-1.461-.977-1.875-2.907-1.016-4.383 1.619 1.986 4.038 3.293 6.766 3.43-.479-2.053 1.08-4.03 3.199-4.03.943 0 1.797.398 2.395 1.037.748-.147 1.451-.42 2.086-.796-.246.767-.766 1.41-1.443 1.816.664-.08 1.297-.256 1.885-.517-.439.656-.996 1.234-1.639 1.697z"}]]
                [:span {:class "sr-only"} "Twitter"]])]]
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.js")]))

(defn get-styles-page
  []
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Styles worksheet"]
    (include-css "/assets/main.css")
    (include-css "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.css")
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin ""}]
    [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&family=Ubuntu:wght@700&display=swap"}]]
   [:body
    (nav/render-main-menu)
    [:div {:class "content"} (example-content)]
    [:footer {:class "footer"}
     [:span "&copy 2023 Cartman Digital Oy"]]
    (include-js "https://cdnjs.cloudflare.com/ajax/libs/flowbite/1.6.5/flowbite.min.js")]))

(defn get-pages
  "Returns map of filename (eg. index.html) -> html" 
  []
  (let [pages-data (contentful/get-contentful :page-collection-query)
        pages (have vector? (get-in pages-data [:pageCollection :items]))]
    (into {"/styles.html" (fn [context] (get-styles-page))}
          (mapv #(vector
                  (if (not= (:slug %) "/") (str "/" (:slug %) ".html") (str "/index.html"))
                  (fn [context](render-page %))) pages))))

(comment
  (println (get-pages)))

(comment
  (render-content {:slug "/",
                   :title "Front page",
                   :sys {:id "5JW0h1Mew6ZQYqIH8zExCx"},
                    :contentCollection
                   {:items
                    [{:__typename "CtaBanner",
                      :cta "This is a example content for the frontpage cta banner",
                      :banner
                      {:title "Founders",
                       :url
                       "https://images.ctfassets.net/038s6vr0kmv0/34YRcoaS4WJ5ORhpMlMHRM/d3500be5d61820cd847513372c22cc2c/IMG_2498__1_.jpg"}}
                     {:__typename "ContentBlock",
                      :content
                      {:json
                       {:nodeType "document",
                        :content
                        [{:nodeType "paragraph",
                          :content
                          [{:nodeType "text",
                            :value "This is the mission statement of cartman and shows up as the first item on the frontpage.",
                            :marks [],
                            :data {}}],
                          :data {}}
                         {:nodeType "paragraph",
                          :content
                          [{:nodeType "text", :value "", :marks [], :data {}}
                           {:nodeType "hyperlink",
                            :content [{:nodeType "text", :value "This is a link from contentful", :marks [], :data {}}],
                            :data {:uri "https://localhost:8000/"}}
                           {:nodeType "text", :value "", :marks [], :data {}}],
                          :data {}}],
                        :data {}}}}
                     {:__typename "ContentBlock",
                      :content
                      {:json
                       {:nodeType "document",
                        :content
                        [{:nodeType "heading-2", :content [{:nodeType "text", :value "About us", :marks [], :data {}}], :data {}}
                         {:nodeType "paragraph",
                          :content
                          [{:nodeType "text",
                            :value
                            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur ut tempor mauris. Mauris tincidunt ac libero commodo ultricies. Duis egestas libero eget tincidunt lobortis. Phasellus cursus pretium pellentesque. Phasellus mattis dui non felis semper finibus. Integer tincidunt dignissim sagittis. Proin augue nunc, commodo eget dolor vel, rhoncus imperdiet eros. Etiam faucibus ut dui sit amet commodo. Aliquam turpis lorem, rhoncus eget erat a, aliquet pharetra purus. Etiam laoreet eget arcu at bibendum. Aenean id sem malesuada lectus luctus imperdiet ut ut erat.",
                            :marks [],
                            :data {}}],
                          :data {}}
                         {:nodeType "paragraph",
                          :content
                          [{:nodeType "text",
                            :value
                            "Vestibulum sit amet metus felis. Maecenas dictum facilisis eleifend. Aenean lectus eros, posuere quis porta a, hendrerit sit amet ante. In hac habitasse platea dictumst. Donec tempor enim non odio fringilla rhoncus. Ut elit augue, aliquet eget eleifend pretium, pulvinar et dolor. Duis pulvinar, lacus commodo laoreet venenatis, mauris felis cursus dolor, nec pretium neque neque et ipsum. Vestibulum egestas scelerisque lacus at malesuada. Mauris ultrices iaculis tortor, a condimentum ligula maximus in. In est nisl, elementum id neque sit amet, convallis elementum libero. Nunc maximus ante consequat mollis interdum. Interdum et malesuada fames ac ante ipsum primis in faucibus.",
                            :marks [],
                            :data {}}],
                          :data {}}],
                        :data {}}}}]}}))