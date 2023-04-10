(ns generator.core
  (:require [stasis.core :as stasis]
            [generator.pages :as pages]))

(def export-directory "./build/")

(defn export []
  (stasis/empty-directory! export-directory)
  (stasis/export-pages (get-pages) export-directory)
  (println)
  (println "Export complete"))