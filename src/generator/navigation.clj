(ns generator.navigation
  (:require [clojure.string :as string]
            [generator.contentful :as contentful]
            [generator.renderer.static :as static]
            [taoensso.truss :as truss :refer (have)]))

(def top-nav "Main menu")

(defn local-url?
  [s]
  (if (string/starts-with? s "http") false true))

(defn frontpage-url?
  [s]
  (if (= (count s) 1) true false))

(defn create-url
  "Convert string to valid uri by prepending base_url and appending .html if url doesn't end with / ."
  [slug]
  (if (local-url? slug)
   (if (frontpage-url? slug)
    (static/prepend-base-url "")
    (static/prepend-base-url (str slug ".html")))
   slug))

(defn render-main-menu []
  (let [menu-data (contentful/get-contentful :nav-collection-query {:name top-nav})
        nav-item-collection (have map? (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
    [:nav {:class "navigation"} 
     [:div {:class "nav-wrapper"}
      [:a {:href (create-url "/") :class "logo"}
       [:img {:src (static/get-asset-url "images/cartman_digital_logo.svg") :title "Cartman Digital"}]
       [:span {:class "sr-only"} "Cartman Digital"]]
      [:button {:data-collapse-toggle "navbar-default" :type "button" :aria-controls "navbar-default" :aria-expanded "false"}
       [:span {:class "sr-only"} "Open main menu"]
       [:svg { :aria-hidden="true" :fill"currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
        [:path  {:fill-rule="evenodd" :d "M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" :clip-rule "evenodd"}]]]
      [:div {:class "nav-list-wrapper hidden" :id "navbar-default"}
       [:ul
        (for [item (:items nav-item-collection)]
          [:li
           [:a {:href (create-url (:slug item))} (get item :title)]])]]]]))
