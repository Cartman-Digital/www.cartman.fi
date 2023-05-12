;; Heavily based on https://github.com/john-shaffer/clj-contentful 
;; Installed into project due to being unable to resolve dependencies
;; From project.clj vs deps.edn configuration
;; Strips out all communication parts from the original code. Rely on existing information.
;; Updates method to return hiccup syntax
(ns generator.renderer
  (:require [hiccup.element :refer (link-to image)]
            [generator.contentful :as contentful]
            [generator.navigation :refer (create-url)]
            [taoensso.truss :as truss :refer (have)]))

;; Workaround to get grid styles compiled into the sheet.
;; Returns one of sm:grid-cols-1 sm:grid-cols-2 sm:grid-cols-3 sm:grid-cols-4
;; sm:grid-cols-5 sm:grid-cols-6
(defn get-grid-class
  [grid-count]
  (str "sm:grid-cols-" grid-count))

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
  (into [:div] (mapv richtext->html (:content m))))

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
  (into [:a {:attrs [:href (:uri (:data m))]}]
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
          (:value m)
          (:marks m)))

(defmethod richtext->html "unordered-list"
  [m]
  (into [:ul] (mapv richtext->html (:content m))))

(defmethod richtext->html "embedded-asset-block"
  [args]
  (if (= (get-in args [:data :target :sys :linkType]) "Asset")
    (let [asset (:asset (contentful/get-contentful :asset-query {:assetId (get-in args [:data :target :sys :id])}))]
      (image (:url asset) (:description asset)))))

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
  [:div {:class "row"}
   [:div {:class "side-by-side left"}
    (richtext->html (get-in args [:leftColumn :json]))]
   [:div {:class "side-by-side right"}
    (richtext->html (get-in args [:rightColumn :json]))]])

(defmethod render "CardList"
 [args]
 (let [cardlist (:cardList (contentful/get-contentful :card-list-query {:listId (get-in args [:sys :id])})) 
       cardCollection (get-in cardlist [:cardsCollection :items])]
   (into [:div {:class (str "card-list row grid " (get-grid-class (:numberOfCardColumns cardlist)))} 
          [:div {:class "intro"} 
           (richtext->html (get-in cardlist [:introduction :json]))]]
         (mapv #(render %) cardCollection))))

(defmethod render "Nav"
 [args]
 (let [menu-data (contentful/get-contentful :nav-collection-query {:name (:name args)})
       nav-item-collection (have map? (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
   [:div {:class "sub-nav"}
    [:ul {:class "grid sm:grid-cols-2 lg:grid-cols-4"}
     (for [item (:items nav-item-collection)]
       [:li {:class (str "nav-item " (:additionalCssClasses item))}
        [:a {:href (create-url (:slug item))} (get item :title)]])]]))

;; Test content
(comment (println (get-in (contentful/get-contentful :card-list-query {:listId "70MjOghTNtz17gmB9BuSZ7"}) [:cardList :cardsCollection :items])))
(comment (render {:__typename "CardList" :sys {:id "2pJ63nhY2QKbVesxgWOvq9"}}))
(comment (richtext->html {:nodeType "paragraph", :content [{:nodeType "text", :value "Testataan", :marks [], :data {}}], :data {}}))
(comment (richtext->html {:nodeType "text", :value "Testataan", :marks [], :data {}}))
(comment (richtext->html {:nodeType "document",
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
                 :data {}}))