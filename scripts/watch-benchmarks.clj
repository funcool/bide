(require '[cljs.build.api :as b])

(b/watch (b/inputs "test" "src")
  {:main 'bide.tests.benchmarks
   :target :nodejs
   :output-to "out/benchmarks.js"
   :output-dir "out/benchmarks"
   :optimizations :none
   :pretty-print true
   :verbose true})
