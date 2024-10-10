(ns generator.sitemap
  (:require
   [generator.contentful :refer [get-contentful]]
   [generator.navigation :refer [create-url]]
   [sitemap.core]
   [sitemap.validator])
  (:import java.io.File))

(defn format-sitemap
  "Prepares a sequence for sitemap.xml"
  [sitemap-page]
  {:loc (create-url (:slug sitemap-page))
   :lastmod (get-in sitemap-page [:sys :publishedAt]) ;; Contentful returns ISO 8601 with format yyyy-MM-dd'T'hh:mm:ss.SSS'Z
   :changefreq (:seoUpdateFrequency sitemap-page)
   :priority (:sitemapPriority sitemap-page)})

  (defn get-custom-items
    "Returns custom sitemap items that are not present in Contentful data"
    []
    (let [post-collection (get-in (get-contentful :last-post-publish-date) [:postCollection :items])]
      [{:slug "articles"
        :seoIndexing true
        :title "Blog"
        :sitemapPriority 0.7
        :sys {:publishedAt (:publishDate (first post-collection))}
        :seoUpdateFrequency "weekly"}]))

(defn get-sitemap-items
  []
  (let [contentful-data (get-contentful :sitemap-query)] 
    (into
     []
     cat 
     [(get-in contentful-data [:pageCollection :items])
      (get-in contentful-data [:postCollection :items])
      #_(get-custom-items)])))

(defn -main
  [target-dir]
  (let [data (get-sitemap-items)
        sitemap-input (map format-sitemap (remove #(false? (:seoIndexing %)) data))
        sitemap (sitemap.core/generate-sitemap sitemap-input)
        validation-result (sitemap.validator/validate-sitemap sitemap)]
    (if (> (count validation-result) 0)
      (throw (IllegalArgumentException. (str (count validation-result) " Errors detected in site-map")))
      (sitemap.core/save-sitemap (File. (str target-dir "sitemap.xml")) sitemap))))


(comment (get-sitemap-items))
