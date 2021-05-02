(defproject funcool/bide "1.7.0"
  :description "Simple routing for ClojureScript"
  :url "https://github.com/funcool/bide"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.10.3" :scope "provided"]
                 [org.clojure/clojurescript "1.10.844" :scope "provided"]]

  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}

  :source-paths ["src" "assets"]
  :test-paths ["test"]
  :jar-exclusions [#"\.swp|\.swo|user.clj"]

  :profiles
  {:dev {:dependencies [[bidi "2.1.4"]
                        [codox-theme-rdash "0.1.2"]]}}


  :codox {:project {:name "bide"}
          :metadata {:doc/format :markdown}
          :language :clojurescript

          :output-path "doc/dist/latest/"
          :doc-paths ["doc/"]
          :themes [:rdash]
          :source-paths ["src"]
          :source-uri "https://github.com/funcool/bide/blob/master/{filepath}#L{line}"
          :namespaces [#"^bide\."]}

  :plugins [[lein-codox "0.10.7"]
            [lein-ancient "0.7.0"]])
