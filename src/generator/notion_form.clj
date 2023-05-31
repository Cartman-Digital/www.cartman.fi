(ns generator.notion-form
  (:require
   [clj-recaptcha.client-v2 :as captcha]
   [generator.config :as config]
   [hiccup.util :as util]
   [lovelace.pages.core :as notion.page]
   [taoensso.truss :as truss]))

(def captcha-private-key (config/get-env "CAPTCHA_PRIVATE_KEY"))

(defn email?
  [email]
  (let [email-pattern #".+@.+\..+"
        valid-email? (re-matches email-pattern email)]
    valid-email?))

(defn sanitize-string
  [s]
  (-> s
      (util/escape-html)))

(defn sanitize
  [m]
  (into {} (for [[k v] m]
             [k (sanitize-string v)])))

(defn validate-fields
  [data]
  
  (truss/have map? data)
  (truss/have [:ks>= #{:name :email :message}] data)
  (truss/have email? (:email data)))

(defn captcha-valid?
  [data]
  (if-let [valid (:valid? (captcha/verify captcha-private-key (:g-captcha data)))]
    valid
    (throw (IllegalArgumentException. "Captcha invalid"))))

;; TODO try catch wrappers and logs
(defn post
  [data]
  (let [{:keys [name email message]} data]
    (notion.page/create-page (config/get-env "NOTION_INTEGRATION_TOKEN") {:parent {:database_id (config/get-env "NOTION_MESSAGE_DB_ID")}
                              :properties {:title {:title [{:text {:content name}}]}
                                           :Email {:email email}
                                           :Message {:rich_text [{:text {:content message}}]}}})))

(defn process-submit
  [data]
  (println (str "captcha pkey: " captcha-private-key))
  (println (str "data: " data))
  (captcha-valid? data)
  (validate-fields data)
  (-> data 
   (sanitize)
   (post)))
