(ns generator.config
  (:require [clojure.edn :as edn]))

(def env-cache (atom {}))

(defn load-sensitive-config []
  (try
    (edn/read-string (slurp "sensitive-config.edn"))
    (catch Exception _ {})))

(defn load-config []
  (try
    (edn/read-string (slurp "generator-config.edn"))
    (catch Exception _ {})))

(defn read-config
  "Read environment configuration"
  [var-name]
  (let [keyword-name (keyword var-name)]
    (or (System/getenv var-name)
        ((load-sensitive-config) keyword-name)
        ((load-config) keyword-name))))

(defn get-env 
  "Cache wrapper for read-config function"
  [var-name] 
  (let [keyword-name (keyword var-name)
        cached-value (@env-cache keyword-name)]
    (or cached-value
        (let [env-value (read-config var-name)]
          (swap! env-cache assoc keyword-name env-value)
          env-value))))


(comment (get-env "CONTENTFUL_SPACE"))