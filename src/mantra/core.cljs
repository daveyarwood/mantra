(ns mantra.core
  (:require [mantra.osc  :as o]
            [mantra.time :as t]
            [mantra.note :as n]))

(def osc             o/osc)
(def start-osc       o/start-osc)
(def stop-osc        o/stop-osc)
(def stop-all-oscs   o/stop-all-oscs)

(def get-tempo       t/get-tempo)
(def set-tempo       t/set-tempo)
(def update-tempo    t/update-tempo)

(def play-note       n/play-note)
(def play-notes      n/play-notes)
(def play-chord      n/play-chord)
(def also-play-note  n/also-play-note)
(def also-play-notes n/also-play-notes)
(def also-play-chord n/also-play-chord)

