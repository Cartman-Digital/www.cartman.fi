(ns generator.pages
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]
            [generator.navigation :as nav]
            [generator.renderer :as renderer]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer (link-to image)]))

(defn render-content 
  [content]
  (map #(renderer/richtext->html (get-in % [:content :json])) (get-in content [:contentCollection :items])))

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
    [:div {:class "mt-24"} (render-content page)]
    [:div {:class "content "} (example-content)] ; (render-content page)
    [:div.footer {:class "footer"}
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