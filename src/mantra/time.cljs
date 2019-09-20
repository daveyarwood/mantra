(ns mantra.time
  (:require [chronoid.core :as c]))

(def ^:dynamic *clock* (atom nil))

(defn create-clock [context]
  (reset! *clock* (doto (c/clock :context context)
                    (c/start!))))

(def ^:dynamic *tempo* (atom 60))

(defn get-tempo [] @*tempo*)

(defn set-tempo [bpm]
  (if bpm
    (reset! *tempo* bpm)
    (throw (js/Error. "You must supply a tempo. e.g.: (set-tempo 120)"))))

(defn update-tempo [f & args]
  (apply swap! *tempo* f args))

