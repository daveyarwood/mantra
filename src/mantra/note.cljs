(ns mantra.note
  (:require [mantra.osc           :as o]
            [mantra.note.duration :as nd]
            [chronoid.core        :as c]))

(defn- play-note*
  [osc-model {:keys [pitch duration volume] :as note-model}]
  (let [{:keys [osc-node gain-node clock] :as osc-impl} (o/osc* osc-model)]
    (o/freq osc-node (or pitch (:freq osc-model)))
    (o/gain-ramp gain-node (or volume (:gain osc-model) 1))

    (o/start-osc osc-impl)

    (when duration
      (let [duration-ms (nd/parse-duration duration)]
        (c/set-timeout! clock #(o/stop-osc osc-impl) duration-ms)))))

(defn play-note
  "Uses a one-off oscillator to play a note.

   Stops any currently playing oscillators belonging to `osc-model`.

   If duration is present, the oscillator is stopped after `duration` ms.

   Otherwise, the note rings out indefinitely, until the oscillator is stopped."
  [osc-model note-model]
  (o/stop-osc osc-model)
  (play-note* osc-model note-model))

(defn also-play-note
  "Like `play-note`, but does not stop any currently playing oscillators."
  [osc-model note-model]
  (play-note* osc-model note-model))

(defn- play-notes*
  [{:keys [clock] :as osc-model} notes play-fn]
  (reduce (fn [timeout {:keys [pitch duration volume] :as note-model}]
            (when pitch
              (c/set-timeout! clock #(play-fn osc-model note-model) timeout))
            (let [duration-ms (nd/parse-duration (or duration 0))]
              (+ timeout duration-ms)))
          0
          notes))

(defn play-notes
  "Plays a sequence of notes, one after the other.

   Stops any currently playing oscillators belonging to `osc-model`.

   If pitch is omitted (or nil), the note is treated as a rest (a pause in the
   sequence, the length of `duration`."
  [osc-model notes]
  ; stop any currently playing oscillators for this osc-model
  ; (even if the first "note" is a rest)
  (o/stop-osc osc-model)
  (play-notes* osc-model notes play-note))

(defn also-play-notes
  "Like `play-notes`, but does not stop any currently playing oscillators.

   Each note in `notes` does behave normally when played, i.e. if it has a
   duration, it *will* stop after `duration` ms."
  [osc-model notes]
  (play-notes* osc-model notes also-play-note))

(defn- play-chord*
  [osc-model notes play-fn]
  (doseq [{:keys [pitch] :as note} notes]
    (when pitch
      (play-fn osc-model note))))

(defn play-chord
  "Plays a collection of notes simultaneously.

   Stops any currently playing oscillators belonging to `osc-model`."
  [osc-model notes]
  (o/stop-osc osc-model)
  (play-chord* osc-model notes also-play-note))

(defn also-play-chord
  "Like `play-chord`, but does not stop any currently playing oscillators.

   Each note in `notes` does behave normally when played, i.e. if it has a
   duration, it *will* stop after `duration` ms."
  [osc-model notes]
  (play-chord* osc-model notes also-play-note))

