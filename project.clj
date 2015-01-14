(defproject hammock "0.2.2"
  :description "tie two trees together to track a transformation"
  :url "https://github.com/shaunlebron/hammock"

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-npm "0.4.0"]]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2665" :scope "provided"]]

  :node-dependencies [[source-map-support "0.2.8"]]

  :source-paths ["src"]

  :clean-targets ["scripts/out"
                  "scripts/hammock.test.js"
                  "scripts/hammock.test.js.map"]

  :cljsbuild {
    :test-commands {"test" ["phantomjs" "scripts/unit-test.js" "scripts/unit-test.html"]}
    :builds [{:id "test"
              :source-paths ["src" "test"]
              :compiler {
                :output-to "scripts/hammock.test.js"
                :output-dir "scripts/out"
                :source-map "scripts/hammock.test.js.map"
                :cache-analysis true                
                :optimizations :whitespace}}]})
