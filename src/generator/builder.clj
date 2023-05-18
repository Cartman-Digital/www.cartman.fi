(ns generator.builder
  (:require [stasis.core :as stasis]
            [generator.pages :as pages]
            [generator.sitemap :as sitemap]))

(def export-directory "./build/")

(defn generate []
  (stasis/empty-directory! export-directory)
  (stasis/export-pages (pages/get-pages) export-directory)
  (sitemap/-main export-directory)
  (println  "Export complete"))