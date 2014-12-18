(defproject hammock "0.1.1"
  :description "tie two trees together to track a transformation"
  :url "https://github.com/shaunlebron/hammock"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2496"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "test"
              :source-paths ["src" "test"]
              :compiler {
                :output-to "script/hammock.test.js"
                :output-dir "script/out"
                :source-map "script/hammock.test.js.map"
                :optimizations :whitespace}}]})
