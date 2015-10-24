(ns mantra.time
  (:require [chronoid.core :as c]))

(def ^:dynamic *clock* (atom nil))

(defn create-clock [context]
  (reset! *clock* (-> (c/clock :context context)
                      (c/start!))))

(def ^:dynamic *tempo* (atom 60))

(defn get-tempo [] @*tempo*)

(defn set-tempo [bpm]
  (reset! *tempo* bpm))

(defn update-tempo [f & args]
  (apply swap! *tempo* f args))

