(defproject nestdown "0.1.0-SNAPSHOT"
  :description "Takes nested (EDN/JSON) data and turns them into markdown"
  :url "https://github.com/latacora/nestdown"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [cheshire "5.8.1"]
                 [com.rpl/specter "1.1.2"]]
  :main ^:skip-aot nestdown.cli
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
