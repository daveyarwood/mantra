(set-env! 
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojurescript "0.0-3308"]
                  [adzerk/bootlaces "0.1.11" :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.0")
(bootlaces! +version+)

(task-options!
  pom {:project 'mantra
       :version +version+
       :description "A ClojureScript library for making music with the Web Audio API"
       :url "https://github.com/daveyarwood/mantra"
       :scm {:url "https://github.com/daveyarwood/mantra"}
       :license {"name" "Eclipse Public License"
                 "url"  "http://www.eclipse.org/legal/epl-v10.html"}})

