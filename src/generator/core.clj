(ns generator.core
  (:require [stasis.core :as stasis]
            [generator.pages :as pages]
            [ring.adapter.jetty :as jetty]) (:gen-class))

(def export-directory "./build/")

(defn -main []
  (stasis/empty-directory! export-directory) 
  (stasis/export-pages (pages/get-pages) export-directory)
  (println)
  (println  "Export complete"))