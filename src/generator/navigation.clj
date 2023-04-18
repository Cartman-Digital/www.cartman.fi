(ns generator.navigation
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]))

(def top-nav "Main menu")

(defn render-main-menu []
  (let [menu-data (contentful/get-contentful :nav-collection-query {:where {:name top-nav}})
        nav-item-collection (have map? (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
    [:nav {:class "border-gray-800 border-b fixed w-full z-20 top-0 left-0"} 
     [:div {:class "max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4"}
      [:a {:href "/" :class "flex items-center"}
       [:img {:src "/assets/images/cartman_digital_logo_nega_small.png" :title "Cartman Digital" :class "h-10 mr-3"}]
       [:span {:class "sr-only self-center text-2xl text-white font-semibold whitespace-nowrap"} "Cartman Digital"]]
      [:button {:data-collapse-toggle "navbar-default" :type "button" :class "inline-flex items-center p-2 ml-3 text-sm md:hidden focus:outline-none focus:ring-2 focus:ring-gray-200 text-gray-400 focus:ring-gray-600" :aria-controls "navbar-default" :aria-expanded "false"}
       [:span {:class "sr-only"} "Open main menu"]
       [:svg {:class "w-6 h-6" :aria-hidden="true" :fill"currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
        [:path  {:fill-rule="evenodd" :d "M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 15a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" :clip-rule "evenodd"}]]]
      [:div {:class "hidden w-full md:block md:w-auto" :id "navbar-default"}
       [:ul {:class "font-medium flex flex-col p-4 md:p-0 mt-4 md:flex-row md:space-x-8 md:mt-0"}
        (for [item (:items nav-item-collection)]
          [:li
           [:a {:href (:slug item) :class "block py-2 pl-3 pr-4 rounded md:border-0 md:p-0 text-white md:hover:text-gray-500 hover:bg-gray-700 md:hover:bg-transparent" } (get item :title)]])]]]]))

(comment
  (render-main-menu))

(comment 
  (get-in test-menu [:navCollection :items 0 :linkedFrom :navItemCollection]))

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