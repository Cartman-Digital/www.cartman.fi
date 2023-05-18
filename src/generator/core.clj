(ns generator.core
  (:require [stasis.core :as stasis]
            [generator.pages :as pages]
            [generator.sitemap :as sitemap]
            [generator.webserver :as webserver]) (:gen-class))

(def export-directory "./build/")

(defn generate []
  (stasis/empty-directory! export-directory)
  (stasis/export-pages (pages/get-pages) export-directory)
  (sitemap/-main export-directory)
  (println  "Export complete"))

(defn -main [& args]
  (let [[flag & remaining-args] args]
    (cond
      (= flag "-generate") (generate)
      (= flag "-webserver") (webserver/start-webserver)
      :else (println "Invalid flag. Supported flags are -generate and -webserver."))))