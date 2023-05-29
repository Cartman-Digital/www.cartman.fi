(ns generator.pages.render
  (:require
   [generator.pages.contact :as page.contact]))

(defmulti get-html
  "Get page specific custom html based on the context of the page."
  (fn [x] (:uri x)))

(defmethod get-html "/contact.html"
 [args]
 (page.contact/get-html))

(defmethod get-html :default
  [args]
  nil)

(comment (get-html {:uri "/contact.html"}))
