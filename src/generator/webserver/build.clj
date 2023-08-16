(ns generator.webserver.build
  (:require
   [generator.builder :refer [generate]]
   [ring.util.response :as response]))

;; Handler for "/api/generate" endpoint 
(defn execute
  "Triggers a build from stasis."
  [request]
  (generate)
  (response/header (response/response "") "Content-Type" "Text/html"))
