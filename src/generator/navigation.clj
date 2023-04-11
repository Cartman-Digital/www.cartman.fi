(ns generator.navigation
  (:require [taoensso.truss :as truss :refer (have)]
            [generator.contentful :as contentful]))

(def top-nav "Main menu")

(defn render-main-menu []
  (let [menu-data (contentful/get-contentful :nav-collection-query {:where {:name top-nav}})
        nav-item-collection (have map? (get-in menu-data [:navCollection :items 0 :linkedFrom :navItemCollection]))]
    [:nav.top-nav
     [:ol
      (for [item (:items nav-item-collection)]
        [:li
         [:a {:href (:slug item)} :title]])]]))



(comment
  (render-main-menu)
  )

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