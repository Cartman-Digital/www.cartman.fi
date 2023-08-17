(ns generator.builder
  (:require
   [generator.pages :as pages]
   [generator.sitemap :as sitemap]
   [stasis.core :as stasis]))

(def export-directory "./build/")

(defn generate []
  (stasis/empty-directory! export-directory)
  (stasis/export-pages (pages/get-pages) export-directory)
  (sitemap/-main export-directory)
  (println  "Export complete")
  (System/exit 0))
