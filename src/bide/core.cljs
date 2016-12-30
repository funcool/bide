;; Copyright (c) 2016-2017 Andrey Antukh <niwi@niwi.nz>
;; All rights reserved.
;;
;; Redistribution and use in source and binary forms, with or without
;; modification, are permitted provided that the following conditions are met:
;;
;; * Redistributions of source code must retain the above copyright notice, this
;;   list of conditions and the following disclaimer.
;;
;; * Redistributions in binary form must reproduce the above copyright notice,
;;   this list of conditions and the following disclaimer in the documentation
;;   and/or other materials provided with the distribution.
;;
;; THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
;; AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
;; IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
;; DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
;; FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
;; DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
;; SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
;; CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
;; OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
;; OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

(ns bide.core
  (:refer-clojure :exclude [empty])
  (:require [bide.impl.router :as rtr]
            [clojure.string :as str]
            [goog.events :as e])
  (:import goog.history.Html5History
           goog.history.EventType))

;; --- Protocols

(defprotocol IRouter
  (-navigate [_ loc params query])
  (-replace-location [_ loc params query]))

(defprotocol IPathRepr
  "Path parameters coercion protocol."
  (-repr [_] "Return a representation of object in path."))

(extend-protocol IPathRepr
  nil
  (-repr [it] "")

  object
  (-repr [it] (str it))

  number
  (-repr [it] it)

  string
  (-repr [it] it)

  cljs.core.Keyword
  (-repr [it] (name it))

  cljs.core.PersistentVector
  (-repr [it]
    (into-array (map -repr it))))

;; --- Low Level Routes API

(defn ^boolean router?
  "Check if the `v` is a Router instance."
  [v]
  (rtr/isRouter v))

(defn empty
  "Construct an empty router."
  []
  (rtr/empty))

(defn insert
  "Insert a new entry to the router."
  [router path name]
  (rtr/insert router path name))

(defn match
  "Try to match a path to a specific route in the router, returns `nil`
  if the no match is found."
  [router path]
  (let [[name params query] (into [] (rtr/match router path))]
    (when name
      [name
       (js->clj params :keywordize-keys true)
       (js->clj query :keywordize-keys true)])))

(defn router
  "A helper for compile a vector of routes in a router instance."
  [routes]
  {:pre [(vector? routes)]}
  (reduce (fn [router [path name]]
            (rtr/insert router path name))
          (rtr/empty)
          routes))

(defn- adapt-params
  [params]
  (when params
    (reduce-kv (fn [m k v]
                 (aset m (key->js k) (-repr v))
                 m)
               (js-obj)
               params)))

(defn resolve
  "Perform a url resolve operation."
  ([router name]
   (resolve router name nil nil))
  ([router name params]
   (resolve router name params nil))
  ([router name params query]
   {:pre [(router? router)]}
   (let [params (adapt-params params)
         query (adapt-params query)]
     (rtr/resolve router name params query))))

;; --- Browser History Binding API

(defn start!
  "Starts the bide routing handling using the goog.History
  api as browser history event watching mechanism.

  If you want use the html5 history instance providing
  a factory function that returns the Html5History object
  instance."
  [router {:keys [on-navigate default html5?]
           :or {html5? false}
           :as opts}]
  (let [default (if (vector? default) default [default nil])]
    (letfn [(-on-navigate [event]
              (let [[name params query] (-match (.-token event))]
                (on-navigate name params query)))
            (-match [token]
              (let [result (match router token)]
                (or result default)))
            (-initial-token [history]
              (let [token (.getToken history)]
                (if (str/blank? token)
                  (or (apply resolve router default) "/")
                  token)))]
      (let [history (if html5?
                      (doto (Html5History.)
                        (.setPathPrefix "")
                        (.setUseFragment false)
                        (.setEnabled true))
                      (doto (Html5History.)
                        (.setUseFragment true)
                        (.setEnabled true)))
            initial-token (-initial-token history)
            initial-loc (-match initial-token)]
        (e/listen history EventType.NAVIGATE -on-navigate)
        (.replaceToken history initial-token)
        (apply on-navigate initial-loc)
        (specify! router
          IRouter
          (-navigate [_ id params query]
            (when-let [path (resolve router id params query)]
              (.setToken history path)))
          (-replace-location [_ id params query]
            (when-let [path (resolve router id params query)]
              (.replaceToken history path))))))))

(defn navigate!
  "Trigger a navigate event to a specific location."
  ([router id] (navigate! router id nil nil))
  ([router id params] (navigate! router id params nil))
  ([router id params query]
   {:pre [(router? router)]}
   (-navigate router id params query)))

(defn replace!
  "Trigger a replace event to a specific location."
  ([router id] (replace! router id nil nil))
  ([router id params] (replace! router id params nil))
  ([router id params query]
   {:pre [(router? router)]}
   (-replace-location router id params query)))
