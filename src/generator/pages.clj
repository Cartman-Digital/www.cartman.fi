(ns generator.pages
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]
            [generator.navigation :as nav]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer (link-to image)]))

(defn render-content [content]
  (into [] [:p "some content"]))

(defn example-content
  "Development tool: prints out significant example content"
  []
  [:div {:class "content-wrapper"}
   [:div {:class "row-wrap clear-left"}
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
     [:div {:class "form-container clear-left pt-10 border border-white pb-2 mb-4 md:w-2/4"}
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
   [:div {:class "hero-banner max-h-96 h-screen bg-gray-800 full-width"}
    [:div {:class "banner-content-wrap content max-w-screen-xl m-auto overflow-hidden"}
     [:div {:class "banner-text w-3/4 lg:w-1/4 float-right mr-14 mt-14"}
      [:h1 {:class "primary-title"} "Banner-title"]
      [:p {:class "banner text content"} "Banner text content. Can contain paragraph that is anywhere from a few words or more to something much much larger. For example multiple paragraphs like this."]
      [:button {:class "cta button action primary"} "Banner-CTA button"]
      [:button {:class "cta button action"} "Banner secondary button"]]]]
   [:div {:class "row-wrap mt-10"}
    [:div {:class "side-by-side md:w-2/4 md:float-left"}
     [:img {:src "/assets/images/cartman_digital_logo.png" :alt "Cartman Digital"}]]
    [:div {:class "side-by-side md:w-2/4 md:float-right md:pl-3"}
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
   [:body {:class "text-white"}
    (nav/render-main-menu)
    [:div {:class "content max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4 mt-16"} (example-content)] ; (render-content page)
    [:div.footer {:class "max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4"}
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