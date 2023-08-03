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

(defn get-sitemap-items
  []
  (let [contentful-data (get-contentful :sitemap-query)] 
    (into 
     (get-in contentful-data [:pageCollection :items])
     (get-in contentful-data [:postCollection :items]))))

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
