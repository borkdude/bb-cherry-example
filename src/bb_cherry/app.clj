(ns bb-cherry.app
  (:require
   [cherry.compiler :as cherry]
   [clojure.template :refer [do-template]]
   [hiccup2.core :refer [html]]
   [org.httpkit.server :as server]))

(def cljs-code
  (do-template
   [from-cljs]
   '(do
      (defn hello [text]
        (js/alert (str text
                       "\n"
                       [:from-cljs (range 20)]
                       "\n"
                       ;; This is calculated at compile time!
                       from-cljs)))
      (-> (js/document.querySelector "#button1")
          (.addEventListener "click" #(hello "This is fine"))))
   [:from-clj (range 20)]))

(def js-str
  (cherry/compile-string (str cljs-code)))

(def module-shim
  [:script {:async true
            :src "https://ga.jspm.io/npm:es-module-shims@1.5.9/dist/es-module-shims.js"}])

(def cherry-import
  [:script {:type "importmap"}
   "{\"imports\": {
     \"cherry-cljs/cljs.core.js\": \"https://cdn.jsdelivr.net/npm/cherry-cljs@0.0.0-alpha.36/cljs.core.js\"}
    }"])

(def compiled-module
  [:script {:type "module"} js-str])

(defn handler [_req]
  (let [body (html
              [:body
               [:button#button1 "Hello"
                ]])
        header (html {:escape-strings? false}
                     [:head
                      module-shim
                      cherry-import
                      compiled-module])
        html-str (str (html [:html header body]))]
    {:body html-str}))

(defn -main
  "Invoke me with clojure -M -m bb-cherry.app"
  [& _args]
  (server/run-server handler {:port 3000})
  (println "App running on port http://localhost:3000")
  @(promise))
