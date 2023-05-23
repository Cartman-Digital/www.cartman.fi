(ns generator.core
  (:require [generator.builder :as builder]
            [generator.webserver :as webserver]) (:gen-class))

(defn -main [& args]
  (let [[flag & remaining-args] args]
    (cond
      (= flag "-generate") (builder/generate)
      (= flag "-webserver") (webserver/start-webserver)
      :else (println "Invalid flag. Supported flags are -generate and -webserver."))))
