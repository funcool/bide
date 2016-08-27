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

;; --- Low Level Routes API

(defn ^boolean router?
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

(defn lookup
  [router path]
  (let [[name params] (into [] (rtr/lookup router path))]
    [name (js->clj params :keywordize-keys true)]))

(defn build
  "Build a router with the provided routes."
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
   (let [params (clj->js params)]
     (rtr/resolve router name params))))

;; --- Browser History Binding API

(defprotocol IRouter
  (-navigare [_ loc params])
  (-replace-location [_ loc params]))

(defn start!
  "Starts the bide routing handling using the goog.History
  api for browser history event watching mechanism."
  [router {:keys [on-navigate default] :as opts}]
  (letfn [(-on-navigate [event]
            (let [[name params] (-lookup (.-token event))]
              (on-navigate name params)))
          (-lookup [token]
            (or (lookup router token) [default nil]))
          (-initial-token [history]
            (let [token (.getToken history)]
              (if-not (str/blank? token)
                token
                (or (resolve (:name default) (:params default)) "/"))))]

  (let [history (doto (History.) (.setEnabled true))
        initial-token (-initial-token history)
        initial-loc (-lookup initial-token)]
    (e/listen history History.EventType.NAVIGATE -on-navigate)
    (.replaceToken history initial-token)
    (on-navigate initial-loc)
    (reify IRouter
      (-navigare [_ location params]
        (.setToken history (resolve location params)))
      (-replace-location [_ location params]
        (.replaceToken history (resolve location params)))))))

(defn navigate!
  "Trigger a navigate event to a specific location."
  ([router name]
   {:pre [(router? router)]}
   (-navigare router name {}))
  ([router name params]
   {:pre [(router? router)]}
   (-navigare router name params)))
