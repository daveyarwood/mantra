(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"test-page"}
  :dependencies   '[[org.clojure/clojure       "1.7.0"]
                    [org.clojure/clojurescript "1.7.228"]
                    [adzerk/bootlaces          "0.1.11" :scope "test"]
                    [adzerk/boot-cljs          "1.7.228-1"]
                    [adzerk/boot-cljs-repl     "0.3.0"  :scope "test"]
                    [com.cemerick/piggieback   "0.2.1"  :scope "test"]
                    [weasel                    "0.7.0"  :scope "test"]
                    [org.clojure/tools.nrepl   "0.2.12" :scope "test"]
                    [chronoid "0.1.0"]])

(require '[adzerk.bootlaces      :refer :all]
         '[adzerk.boot-cljs      :refer (cljs)]
         '[adzerk.boot-cljs-repl :refer (cljs-repl start-repl)])

(def +version+ "0.5.1")
(bootlaces! +version+)

(task-options!
  pom {:project 'mantra
       :version +version+
       :description "A ClojureScript library for making music with the Web Audio API"
       :url "https://github.com/daveyarwood/mantra"
       :scm {:url "https://github.com/daveyarwood/mantra"}
       :license {"name" "Eclipse Public License"
                 "url"  "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev []
  (comp (watch)
        (speak)
        (cljs-repl)
        (cljs)
        (target)))

(deftask deploy
  "Builds uberjar, installs it to local Maven repo, and deploys it to Clojars."
  []
  (comp (build-jar) (push-release)))
