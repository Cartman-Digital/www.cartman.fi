(ns generator.sitemap
  (:require
   [generator.contentful :refer [get-contentful]]
   [generator.navigation :refer [create-url]]
   [sitemap.core]
   [sitemap.validator])
  (:import java.io.File))

(def base-url "https://cartman.fi")

(defn slug-to-url
  "Creates the url by appending .html extension, then adds leading / character and finally combines it with defined base-url"
  [slug]
  (let [url (create-url slug)
        prefixed-url (if (= (take-last 1 url) '(\/)) url (str "/" url))]
    (str base-url prefixed-url)))

(defn format-sitemap
  "Prepares a sequence for sitemap.xml"
  [sitemap-page]
  {:loc (slug-to-url (:slug sitemap-page))
   :lastmod (get-in sitemap-page [:sys :publishedAt]) ;; Contentful returns ISO 8601 with format yyyy-MM-dd'T'hh:mm:ss.SSS'Z
   :changefreq (:seoUpdateFrequency sitemap-page)
   :priority (:sitemapPriority sitemap-page)})

(defn -main 
  [target-dir]
  (let [sitemap-input (map format-sitemap (get-in (get-contentful :sitemap-query) [:pageCollection :items]))
        sitemap (sitemap.core/generate-sitemap sitemap-input)
        validation-result (sitemap.validator/validate-sitemap sitemap)] 
    (if (> (count validation-result) 0) 
      (throw (IllegalArgumentException. (str (count validation-result) " Errors detected in site-map")))
      (sitemap.core/save-sitemap (File. (str target-dir "sitemap.xml")) sitemap ))))
