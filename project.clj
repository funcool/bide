(defproject funcool/bide "1.7.0"
  :description "Simple routing for ClojureScript"
  :url "https://github.com/funcool/bide"
  :license {:name "BSD (2-Clause)"
            :url "http://opensource.org/licenses/BSD-2-Clause"}

  :source-paths ["src" "assets"]
  :test-paths ["test"]
  :jar-exclusions [#"\.swp|\.swo|user.clj"]
  :jar-name "bide.jar"

  :profiles
  {:dev {:dependencies [[bidi "2.1.4"]
                        [org.clojure/clojure "1.10.3" :scope "provided"]
                        [org.claojure/clojurescript "1.10.844" :scope "provided"]]}})
