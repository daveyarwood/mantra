(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"test-page"}
  :dependencies   '[[org.clojure/clojure       "1.10.1"       :scope "provided"]
                    [org.clojure/clojurescript "1.10.520"     :scope "provided"]
                    [io.djy/chronoid           "0.2.0"]
                    [music-theory              "0.1.0"]
                    [adzerk/bootlaces          "0.2.0"        :scope "test"]
                    [adzerk/boot-cljs          "2.1.5"        :scope "test"]
                    [adzerk/boot-cljs-repl     "0.4.0"        :scope "test"]
                    [cider/piggieback          "0.4.1"        :scope "test"]
                    [weasel                    "0.7.0"        :scope "test"]
                    [nrepl                     "0.7.0-alpha1" :scope "test"]
                    ;; Workaround for boot-cljs-repl using something that was
                    ;; removed in JDK 11. This also seems to fail when I use JDK
                    ;; 8, which I don't understand, but anyway, this fixes it.
                    [javax.xml.bind/jaxb-api "2.4.0-b180830.0359" :scope "test"]])

(require '[adzerk.bootlaces      :refer :all]
         '[adzerk.boot-cljs      :refer (cljs)]
         '[adzerk.boot-cljs-repl :refer (cljs-repl start-repl)])

(def +version+ "0.6.1")
(bootlaces! +version+)

(task-options!
  pom {:project 'io.djy/mantra
       :version +version+
       :description "A ClojureScript library for making music with the Web Audio API"
       :url "https://github.com/daveyarwood/mantra"
       :scm {:url "https://github.com/daveyarwood/mantra"}
       :license {"name" "Eclipse Public License"
                 "url"  "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev []
  (comp (watch)
        #_(speak)
        (cljs-repl)
        (cljs)
        (target)))

(deftask deploy
  "Builds uberjar, installs it to local Maven repo, and deploys it to Clojars."
  []
  (comp (build-jar) (push-release)))
