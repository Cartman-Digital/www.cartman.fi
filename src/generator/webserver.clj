(ns generator.webserver
 (:require 
    ;; [ring.middleware.content-type :refer [wrap-content-type]] leiningen dependency used to wrap asset calls: unable to load in our project 
    [stasis.core :as stasis]
    [optimus.prime :as optimus]
    [optimus.assets :as assets]
    [optimus.optimizations :as optimizations]
    [optimus.strategies :refer [serve-live-assets]]
    [optimus.export]
    [generator.pages :refer [get-pages]]))

(defn get-assets []
  (assets/load-assets "public" ["/assets/main.css" 
                                #"/assets/images/.*\.png"]))

;; ring middleware dependency would be used in the "optimus/wrap" call, wrap-content-type would be the last param in optimus/wrap
(def app (-> (stasis/serve-pages get-pages) ;; should return map of slug -> render call
             (optimus/wrap get-assets optimizations/all serve-live-assets)))