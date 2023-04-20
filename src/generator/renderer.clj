;; Heavily based on https://github.com/john-shaffer/clj-contentful 
;; Installed into project due to being unable to resolve dependencies
;; From project.clj vs deps.edn configuration
;; Strips out all communication parts from the original code. Rely on existing information.
;; Updates method to return hiccup syntax
(ns generator.renderer)

(defn unwrap
  "Takes the results from rich-text conversion and unwraps the sequence if it has only a single element"
  [seq]
  (if (= (count seq) 1)
    (first seq)
    seq))

;; https://github.com/contentful/rich-text/blob/master/packages/rich-text-types/src/marks.ts
;; Used automatically by richtext->html
(defmulti apply-text-mark
  (fn [mark content]
    mark))

(defmethod apply-text-mark "bold"
  [mark content]
  [:strong content])

(defmethod apply-text-mark "code"
  [mark content]
  [:code content])

(defmethod apply-text-mark "italic"
  [mark content]
  [:em content])

(defmethod apply-text-mark "underline"
  [mark content]
  [:u content])

;; Entry-point expects richtext mapv, first level must contain nodeType key.
(defmulti richtext->html :nodeType)

(defmethod richtext->html :default
  [m]
  m)

(defmethod richtext->html "blockquote"
  [m]
  [:blockquote (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "document"
  [m]
  [:div (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "heading-1"
  [m]
  [:h1 (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "heading-2"
  [m]
  [:h2 (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "heading-3"
  [m]
  [:h3 (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "heading-4"
  [m]
  [:h5 (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "heading-5"
  [m]
  [:h5 (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "heading-6"
  [m]
  [:h6 (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "hr"
  [m]
  [:hr])

(defmethod richtext->html "hyperlink"
  [m]
  [:a
   :attrs [:href (:uri (:data m))]
   (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "list-item"
  [m]
  [:li (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "ordered-list"
  [m]
  [:ol (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "paragraph"
  [m]
  [:p (unwrap(mapv richtext->html (:content m)))])

(defmethod richtext->html "text"
  [m]
  (reduce #(apply-text-mark (:type %2) %)
          (:value m)
          (:marks m)))

(defmethod richtext->html "unordered-list"
  [m]
  [:ul (unwrap(mapv richtext->html (:content m)))])

;; Test content
(richtext->html {:nodeType "paragraph", :content [{:nodeType "text", :value "Testataan", :marks [], :data {}}], :data {}})
;(richtext->html {:nodeType "text", :value "Testataan", :marks [], :data {}})
