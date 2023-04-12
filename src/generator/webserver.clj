(ns generator.webserver
 (:require
    [stasis.core :as stasis]
    [generator.pages :as pages]))

(def app (stasis/serve-pages pages/get-pages))