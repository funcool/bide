(ns bide.tests.benchmarks
  (:require [bide.core :as bide]
            [bidi.bidi :as bidi]))

(enable-console-print!)

(def bidi-routes
  ["/" [["auth/login" :auth/login]
        [["auth/recovery/token/" :token] :auth/recovery]
        ;; ["settings/" [["profile" :settings/profile]
        ;;               ["password" :settings/password]
        ;;               ["notifications" :settings/notifications]]]
        ["workspace/" [[[:project "/" :page] :workspace/page]]]]])

(def bide-routes
  (bide/build
   [["/auth/login" :auth/login]
    ["/auth/recovery/token/:token" :auth/recovery]
    ;; ["/settings/profile" :settings/profile]
    ;; ["/settings/password" :settings/password]
    ;; ["/settings/notifications" :settings/notifications]
    ["/workspace/:project/:page" :workspace/page]]))

(defn benchmark-bidi-resolve
  [ops]
  ;; Check
  ;; (println (bidi/path-for bidi-routes :workspace/page :project 1 :page 1))

  ;; Warm Up
  (dotimes [i ops]
    (bidi/path-for bidi-routes :workspace/page :project 1 :page 1))

  (time
   (dotimes [i ops]
     (bidi/path-for bidi-routes :workspace/page :project 1 :page 1))))

(defn benchmark-bide-resolve
  [ops]
  ;; Check
  ;; (println (bide/resolve bide-routes :workspace/page {:project 1 :page 1}))

  ;; Warm Up
  (dotimes [i ops]
    (bide/resolve bide-routes :workspace/page {:project 1 :page 1}))

  (time
   (dotimes [i ops]
     (bide/resolve bide-routes :workspace/page {:project 1 :page 1}))))

(defn benchmark-bidi-match
  [ops]
  ;; Check
  ;; (println
  ;;  (bidi/match-route bidi-routes "/workspace/1/1"))

  ;; Warn Up
  (dotimes [i ops]
    (bidi/match-route bidi-routes "/workspace/1/1"))

  (time
   (dotimes [i ops]
     (bidi/match-route bidi-routes "/workspace/1/1"))))

(defn benchmark-bide-match
  [ops]
  ;; Check
  ;; (println
  ;;  (bide/match bide-routes "/workspace/1/1"))

  ;; Warm Up
  (dotimes [i ops]
    (bide/match bide-routes "/workspace/1/1"))

  (time
   (dotimes [i ops]
     (bide/match bide-routes "/workspace/1/1"))))

(defn main
  [& args]
  (do
    (println "op=resolve lib=bidi ops=10000")
    (benchmark-bidi-resolve 10000))

  (do
    (println "op=resolve lib=bide ops=10000")
    (benchmark-bide-resolve 10000))

  (do
    (println "op=match lib=bidi ops=10000")
    (benchmark-bidi-match 10000))

  (do
    (println "op=match lib=bide ops=10000")
    (benchmark-bide-match 10000)))

(set! *main-cli-fn* main)


