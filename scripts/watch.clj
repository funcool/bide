(require '[cljs.build.api :as b])

(b/watch (b/inputs "test" "src")
  {:main 'bide.tests.core-tests
   :target :nodejs
   :output-to "out/tests.js"
   :output-dir "out"
   :optimizations :none
   :pretty-print true
   :verbose true})
