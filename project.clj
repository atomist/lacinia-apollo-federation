(defproject com.atomist/lacinia-apollo-federation "0.1.1-SNAPSHOT"
  :description "Apollo Federation helper for Lacinia"
  :url "https://github.com/atomisthq/lacinia-apollo-federation"
  :license {:name "Apache License 2.0"
            :url "https://opensource.org/licenses/Apache-2.0"}
  :dependencies [[com.walmartlabs/lacinia "0.36.0-alpha-2"]]
  :repl-options {:init-ns lacinia-apollo-federation.core}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
