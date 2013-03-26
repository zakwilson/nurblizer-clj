(ns nurblizer.core
  (:gen-class :name nurblizer.core)
  (:use compojure.core nurblizer.helpers)
  (:require
    [clojure.string :as str]
    [ring.adapter.jetty :as ring]
    [compojure.core :as compojure]
    [compojure.route :as route]
    [compojure.handler :as handler]))


; Read in the nouns file on startup
(def nouns
  (set (map (comp str/trim str/lower-case)
                  (-> (slurp (clojure.java.io/resource "nouns.txt"))
                      (str/split #"\n")))))


; Nurblize function: now using a set!
(defn nurble [text]
  (let [words (-> text
                  str/lower-case
                  (str/replace #"[^a-z \n]" "")
                  (str/split #"\b"))]
    (->> (for [w words]
           (cond (nouns w) "<span class=\"nurble\">nurble</span>"
                 true (->> w
                           (replace {\newline "<br />"})
                           (apply str)
                           (str/upper-case))))
         (interpose \space)
         (apply str))))

; Handlers
(defn index-view []
  (render "index" {}))

(defn nurble-view [text]
  (render "nurble" {:text (nurble text)}))


; Routes
(defroutes main-routes
  (GET "/" [] (index-view))
  (POST "/nurble" [text] (nurble-view text))
  (route/resources "/static"))


; And finally, the server itself
(defn -main []
  (ring/run-jetty (handler/site main-routes) {:port 9000}))
