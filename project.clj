(defproject frontend "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring-server "0.4.0"]
                 [reagent "0.6.1"]
                 [reagent-utils "0.2.1"]
                 [ring "1.5.1"]
                 [ring/ring-defaults "0.2.3"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-anti-forgery "1.1.0"]
                 [digest "1.4.6"]
                 [compojure "1.5.2"]
                 [hiccup "1.0.5"]
                 [garden "1.3.2"]
                 [yogthos/config "0.8"]
                 [yesql "0.5.3"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.clojure/clojurescript "1.9.521"
                  :scope "provided"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.0"
                  :exclusions [org.clojure/tools.reader]]
                 [datascript "0.16.1"]
                 [environ "1.1.0"]
                 [cljs-http "0.1.43"]]

  :plugins [[lein-environ "1.0.2"]
            [lein-ring "0.9.7"]
            [lein-cljsbuild "1.1.5"]
            [lein-garden "0.2.8"]
            [lein-asset-minifier "0.2.7"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler frontend.handler/app
         :uberwar-name "frontend.war"}



  :garden {:builds [{:source-paths ["src/cljs"]
                     :stylesheet frontend.styles/style
                     :compiler {:output-to "resources/public/css/app.css"}}]}

  :min-lein-version "2.5.0"

  :uberjar-name "stackomat.jar"

  :main frontend.server

  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"
            "resources/public/css/app.min.css" "resources/public/css/app.css"}}

  :cljsbuild {:builds {:min {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
                             :compiler
                             {:output-to "target/cljsbuild/public/js/app.js"
                              :output-dir "target/uberjar"
                              :optimizations :advanced
                              :pretty-print  false}}
                       :app {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                             :figwheel {:on-jsload "frontend.core/mount-root"
                                        :websocket-host :js-client-host}
                             :compiler
                             {:main "frontend.dev"
                              :asset-path "/js/out"
                              :output-to "target/cljsbuild/public/js/app.js"
                              :output-dir "target/cljsbuild/public/js/out"
                              :source-map true
                              :optimizations :none
                              :pretty-print  true}}}}

  :figwheel {:http-server-root "public"
             :hawk-options {:watcher :polling}
             :server-port 3449
             :nrepl-port 7002
             :nrepl-middleware ["cemerick.piggieback/wrap-cljs-repl"]
             :css-dirs ["resources/public/css"]
             :ring-handler frontend.handler/app}

  :profiles {:dev {:repl-options {:init-ns frontend.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.5.1"]
                                  [prone "1.1.4"]
                                  [figwheel-sidecar "0.5.10"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [com.cemerick/piggieback "0.2.2-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.8.1"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.13"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
