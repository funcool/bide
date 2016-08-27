(require '[cljs.build.api :as b])

(println "Building ...")

(let [start (System/nanoTime)]
  (b/build
   (b/inputs "test" "src")
   {:main 'bide.tests.core-tests
    :output-to "out/tests.js"
    :output-dir "out/tests"
    :target :nodejs
    :optimizations :advanced
    :pretty-print true
    :verbose true})
  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds"))
