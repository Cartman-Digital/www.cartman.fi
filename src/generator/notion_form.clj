(ns generator.notion-form
  (:require
   [clj-recaptcha.client-v2 :as captcha]
   [generator.config :as config]
   [hiccup.util :as util]
   [lovelace.pages.core :as notion.page]
   [taoensso.truss :as truss]))

(def captcha-private-key (config/get-env "CAPTCHA_PRIVATE_KEY"))
(def notion-integration-token (config/get-env "NOTION_INTEGRATION_TOKEN"))
(def notion-message-db-id (config/get-env "NOTION_MESSAGE_DB_ID"))

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
  (let [result (captcha/verify captcha-private-key (:g-captcha data))]
    (if (:valid? result)
      true
      (throw (IllegalArgumentException. (str "Captcha invalid: " result))))))

(defn post
  [data]
  (let [{:keys [name email message]} data
        result (notion.page/create-page notion-integration-token {:parent {:database_id notion-message-db-id}
                                                                  :properties {:title {:title [{:text {:content name}}]}
                                                                               :Email {:email email}
                                                                               :Message {:rich_text [{:text {:content message}}]}}})]
    (println result)
    (when (:error result) (throw (ex-info "Failed to POST data" {:error (:error result)})))))

(defn process-submit
  [data]
  (captcha-valid? data)
  (validate-fields data)
  (-> data 
   (sanitize)
   (post)))
