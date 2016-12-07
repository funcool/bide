;; Copyright (c) 2016 Andrey Antukh <niwi@niwi.nz>
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
  (:import goog.History))

;; --- Protocols

(defprotocol IRouter
  (-navigare [_ loc params])
  (-replace-location [_ loc params]))

(defprotocol IPathRepr
  "Path parameters coercion protocol."
  (-repr [_] "Return a representation of object in path."))

(extend-protocol IPathRepr
  nil    (-repr [it] "")
  object (-repr [it] (str it))
  number (-repr [it] it)
  string (-repr [it] it)
  cljs.core.Keyword (-repr [it] (name it)))

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
            (assert (string? path) "path should be string")
            (assert (keyword? name) "name should be keyword")
            (rtr/insert router path name))
          (rtr/empty) routes))

(defn resolve
  "Perform a url resolve operation."
  ([router name]
   (resolve router name {}))
  ([router name params]
   {:pre [(router? router)]}
   (let [params (reduce-kv (fn [m k v]
                             (aset m (key->js k) (-repr v))
                             m)
                           (js-obj)
                           params)]
     (rtr/resolve router name params))))

;; --- Browser History Binding API

(defn start!
  "Starts the bide routing handling using the goog.History
  api as browser history event watching mechanism."
  [router {:keys [on-navigate default] :as opts}]
  (let [default (if (vector? default) default [default nil])]
    (letfn [(-on-navigate [event]
              (let [[name params] (-match (.-token event))]
                (on-navigate name params)))
            (-match [token]
              (let [result (match router token)]
                (or result default)))
            (-initial-token [history]
              (let [token (.getToken history)]
                (if (str/blank? token)
                  (or (apply resolve router default) "/")
                  token)))]
      (let [history (doto (History.) (.setEnabled true))
            initial-token (-initial-token history)
            initial-loc (-match initial-token)]
        (e/listen history History.EventType.NAVIGATE -on-navigate)
        (.replaceToken history initial-token)
        (apply on-navigate initial-loc)
        (specify! router
          IRouter
          (-navigare [_ location params]
            (when-let [path (resolve router location params)]
              (.setToken history path)))
          (-replace-location [_ location params]
            (when-let [path (resolve router location params)]
              (.replaceToken history path))))))))

(defn navigate!
  "Trigger a navigate event to a specific location."
  ([router name]
   {:pre [(router? router)]}
   (-navigare router name {}))
  ([router name params]
   {:pre [(router? router)]}
   (-navigare router name params)))
