(ns generator.webserver.contact
  (:require
   [generator.notion-form :as contact]
   [ring.middleware.params :as ring.params]
   [ring.util.response :as response]))

(defn execute
  "Takes in contact form request and forwards it to notion form integration."
  [request]
  (let [parsed-request (ring.params/params-request request)
        {:strs [name email message g-recaptcha-response]} (:form-params parsed-request)]
    (contact/process-submit {:name name
                             :email email
                             :message message
                             :g-captcha g-recaptcha-response})
    (response/header (response/response "Your message has been received. We will respond to you as soon as we can.") "Content-Type" "Text/html")))
 