;; Heavily based on https://github.com/john-shaffer/clj-contentful 
;; Installed into project due to being unable to resolve dependencies
;; From project.clj vs deps.edn configuration
;; Strips out all communication parts from the original code. Rely on existing information.
;; Updates method to return hiccup syntax
(ns generator.renderer)

; https://github.com/contentful/rich-text/blob/master/packages/rich-text-types/src/marks.ts
(defmulti apply-text-mark
  (fn [mark content]
    mark))

(defmethod apply-text-mark "bold"
  [mark content]
  [:strong
    [content]])

(defmethod apply-text-mark "code"
  [mark content]
  [:code [content]])

(defmethod apply-text-mark "italic"
  [mark content]
  [:em [content]])

(defmethod apply-text-mark "underline"
  [mark content]
  [:u [content]])

(defmulti richtext->html :nodeType)

(defmethod richtext->html :default
  [m]
  m)

(defmethod richtext->html "blockquote"
  [m]
  [:blockquote (map richtext->html (:content m))])

(defmethod richtext->html "document"
  [m]
  [:div (map richtext->html (:content m))])

(defmethod richtext->html "heading-1"
  [m]
  [:h1 (map richtext->html (:content m))])

(defmethod richtext->html "heading-2"
  [m]
  [:h2 (map richtext->html (:content m))])

(defmethod richtext->html "heading-3"
  [m]
  [:h3 (map richtext->html (:content m))])

(defmethod richtext->html "heading-4"
  [m]
  [:h5 (map richtext->html (:content m))])

(defmethod richtext->html "heading-5"
  [m]
  [:h5 (map richtext->html (:content m))])

(defmethod richtext->html "heading-6"
  [m]
  [:h6 (map richtext->html (:content m))])

(defmethod richtext->html "hr"
  [m]
  [:hr])

(defmethod richtext->html "hyperlink"
  [m]
  [:a
   :attrs [:href (:uri (:data m))]
   (map richtext->html (:content m))])

(defmethod richtext->html "list-item"
  [m]
  [:li (map richtext->html (:content m))])

(defmethod richtext->html "ordered-list"
  [m]
  [:ol (map richtext->html (:content m))])

(defmethod richtext->html "paragraph"
  [m]
  [:p (map richtext->html (:content m))])

(defmethod richtext->html "text"
  [m]
  (reduce #(apply-text-mark (:type %2) %)
          (:value m)
          (:marks m)))

(defmethod richtext->html "unordered-list"
  [m]
  [:ul (map richtext->html (:content m))])