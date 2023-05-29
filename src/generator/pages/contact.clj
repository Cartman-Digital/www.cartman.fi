(ns generator.pages.contact
  (:require 
   [generator.config :as config]
   [hiccup.form :as form]))

(defn get-captcha
  []
  [:div
   [:script {:type "text/javascript", :src "https://www.google.com/recaptcha/api.js" :defer "" :async ""}]
   [:button.button.action.primary.g-recaptcha {:data-sitekey (config/get-env "CAPTCHA_PUBLIC_KEY") :data-callback "onSubmit"} "Contact us"]
   [:script {:type "text/javascript"} "function onSubmit(token) {
         document.getElementById(\"contact-form\").submit();
       }"]])

(defn get-html
  []
  [:div.row 
   [:div.form-wrap
    [:h1 "Contact us"]
    [:form#contact-form {:method "post" :action "api/contact"}
     (form/label "name" "What is your name?")
     (form/text-field "name")
     (form/label "email" "What is your email?")
     (form/email-field "email")
     (form/label "message" "How can we help you?")
     (form/text-area "message")
     (get-captcha)]]])
