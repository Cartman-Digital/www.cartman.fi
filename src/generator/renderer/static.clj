(ns generator.renderer.static 
  (:require [hiccup.page :as page]
            [generator.config :as config]))

(def assets-version (apply str "v/" (repeatedly 5 #(rand-int 9))))

(defn prepend-base-url
  "Adds base url to url path given, if url doesn't start with \"http\"."
  [url]
  (str (config/get-env "BASE_URL") url))

(defn get-version-path
  ([path type] (str "assets/" assets-version "/" type "/" path))
  ([path] (str "assets/" assets-version "/" path)))

(defn get-local-css
  "Adds css when called from hiccup html output. Path contains version number."
  [filename]
  (page/include-css (get-version-path filename)))

(defn get-local-js
  "Adds js when called from hiccup html output. Path contains version number."
  [filename]
  (page/include-js (prepend-base-url (get-version-path filename "js"))))

(defn get-asset-url
  [asset-path]
  (prepend-base-url (get-version-path asset-path)))
