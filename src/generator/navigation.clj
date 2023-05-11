(ns generator.navigation
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]))

(def top-nav "Main menu")

(defn create-url
  [slug]
  (if (= (take-last 1 slug) '(\/)) slug (str slug ".html")))

(defn create-root-url
  [slug]
  (let [url (create-url slug)]
    (if (= (take-last 1 url) '(\/)) url (str "/" url))))

(defn render-main-menu []
  (let [menu-data (contentful/get-contentful :nav-collection-query {:name top-nav})
        nav-item-collection (have map? (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
    [:nav {:class "navigation"} 
     [:div {:class "nav-wrapper"}
      [:a {:href "/" :class "logo"}
       [:img {:src "/assets/images/cartman_digital_logo.svg" :title "Cartman Digital"}]
       [:span {:class "sr-only"} "Cartman Digital"]]
      [:button {:data-collapse-toggle "navbar-default" :type "button" :aria-controls "navbar-default" :aria-expanded "false"}
       [:span {:class "sr-only"} "Open main menu"]
       [:svg { :aria-hidden="true" :fill"currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
        [:path  {:fill-rule="evenodd" :d "M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" :clip-rule "evenodd"}]]]
      [:div {:class "nav-list-wrapper hidden" :id "navbar-default"}
       [:ul
        (for [item (:items nav-item-collection)]
          [:li
           [:a {:href (create-root-url (:slug item))} (get item :title)]])]]]]))

(comment 
  (get-in test-menu [:navCollection :items 0 :linkedFrom :navItemCollection]))
(comment (render-main-menu))
(comment (contentful/get-contentful :nav-collection-query {:name "Services sub menu"}))
(comment (def test-menu
  {:navCollection
   {:items
    [{:linkedFrom
      {:navItemCollection
       {:total 4,
        :items
        [{:linkedFrom {:navItemCollection {:total 0, :items []}},
          :slug "contact",
          :title "Contact Us!",
          :weight 0,
          :sys {:id "zwU3neiz0x9JUahuZry32"}}
         {:linkedFrom
          {:navItemCollection
           {:total 1,
            :items
            [{:slug "solutions",
              :title "Solutions",
              :weight 0,
              :sys {:id "jVrq1P921CCgV1aGvRplA"}}]}},
          :slug "services",
          :title "Services",
          :weight 50,
          :sys {:id "6oMuk7XZZsaCOsB3YbmRs3"}}
         {:linkedFrom {:navItemCollection {:total 0, :items []}},
          :slug "/",
          :title "Home",
          :weight 100,
          :sys {:id "54Gk668aUSpXVAPblu7YXy"}}
         {:linkedFrom {:navItemCollection {:total 0, :items []}},
          :slug "team",
          :title "Team",
          :weight nil,
          :sys {:id "wkxC50cUABwCR73bjK7sX"}}]}},
      :name "Main menu",
      :sys {:id "gbZlKpR4jlgOkdEdSqYvW"}}]}}))