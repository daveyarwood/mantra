(ns mantra.core
  (:require [chronoid.core :as c]))

(def ^:dynamic *audio-context* (atom nil))
(def ^:dynamic *clock* (atom nil))

(defn create-audio-context [] 
  (let [constructor (or js/window.AudioContext
                        js/window.webkitAudioContext)]
    (reset! *audio-context* (constructor.))))

(defn create-clock [context]
  (reset! *clock* (-> (c/clock :context context)
                      (c/start!))))

(defn osc 
  "Models the state of an oscillator.
   
   e.g. (osc :type :square
             :freq 440
             :gain 0.5)
   
   This produces a map that is used to create a fresh oscillator every time
   one is needed. Because oscillators can only be started once, this
   provides an abstraction for a 'persistent oscillator' that can be reused 
   any number of times (but under the hood, it's just a blueprint for any
   number of oscillators).
   
   :type, :freq and :gain are all optional.

   :type defaults to a sine wave.

   :freq and :gain are optional default values that, when present, are used
   when playing notes that do not specify frequency/gain values. If left out,
   they default to 440 Hz and 1.0 (full volume), respectively.
   
   You can also supply your own AudioContext as :context, otherwise Mantra will
   create and use its own AudioContext.
   
   Similarly, you can supply your own chronoid clock as :clock, otherwise 
   Mantra will create one for you."
  [& {:keys [type freq gain context clock] :as osc-map}]
  (let [type    (name (or type :sine))
        context (or context @*audio-context* (create-audio-context))]
    (if-not (contains? #{"sine" "square" "sawtooth" "triangle"} type)
      (throw (js/Error. (str type " is not a valid oscillator type.")))
      (assoc osc-map :type    type
                     :id      (gensym type)
                     :context context
                     :clock   (or clock @*clock* (create-clock context))))))

(defn- osc*
  "Creates a one-off oscillator based on a map, hooks it up to a gain node, 
   and hooks the gain node up to the destination of the AudioContext.

   Returns a map including the oscillator and gain nodes and the ID of the
   oscillator model used as a blueprint."
  [{:keys [id type freq context clock]}]
  (let [osc  (.createOscillator context)
        gain (.createGain context)]
    (set! (.-type osc) (or type "sine"))
    (set! (.-value (.-frequency osc)) (or freq 440))
    (set! (.-value (.-gain gain)) 0)
    (.connect osc gain)
    (.connect gain (.-destination context))
    {:osc-node  osc
     :gain-node gain
     :model-id  id
     :context   context
     :clock     clock}))

(def ^{:doc "A set of currently active oscillators. Each oscillator is 
             represented as a map containing:

             :osc-node  -- the oscillator node
             :gain-node -- the gain node it's connected to
             :model-id  -- the ID of the model that was used as a 
             blueprint to create the oscillator"}
  *oscillators* (atom #{}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- freq [osc-node hz]
  (set! (.-value (.-frequency osc-node)) hz))

(defn- gain [gain-node level]
  (set! (.-value (.-gain gain-node)) level))

(defn- gain-ramp [gain-node level]
  (let [time (.-currentTime (.-context gain-node))]
    (.exponentialRampToValueAtTime (.-gain gain-node) level (+ time 0.1))))

(defn- silence [gain-node]
  (gain gain-node 0))

(defn- silence-ramp [gain-node]
  (gain-ramp gain-node 0.0001))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn start-osc
  "Start an oscillator and add it to *oscillators*."
  [{:keys [osc-node] :as osc-impl}]
  (.start osc-node 0)
  (swap! *oscillators* conj osc-impl))

(defn stop-osc
  "Silences and stops a currently playing oscillator.
   
   This fn can take either an osc-model or an osc-impl as an argument."
  [osc]
  (cond
    ; osc-impl
    (contains? osc :model-id)
    (let [{:keys [osc-node gain-node clock]} osc]
      (silence-ramp gain-node)
      (c/set-timeout! clock #(do
                               (swap! *oscillators* disj osc)
                               (.stop osc-node)) 
                      1000))
    ; osc-model
    (contains? osc :id)
    (doseq [osc-impl (filter #(= (:id osc) (:model-id %)) @*oscillators*)]
      (stop-osc osc-impl))))

(defn stop-all-oscs
  "Silences and stops all currently playing oscillators."
  []
  (doseq [osc @*oscillators*]
    (stop-osc osc)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *tempo* (atom 60))

(defn get-tempo [] @*tempo*)

(defn set-tempo [bpm]
  (reset! *tempo* bpm))

(defn update-tempo [f & args]
  (apply swap! *tempo* f args))

(defn dotted 
  "Returns the theoretical new note length when a note-length is given the 
   specified number of dots. 

   e.g. given that quarter = 4, dotted quarter = 2-2/3"
  [note-length dots]
  (let [beats (/ 4 note-length)]
    (loop [total-beats beats, factor 0.5, dots dots]
      (if (pos? dots)
        (recur (+ total-beats (* beats factor)) (* factor 0.5) (dec dots))
        (/ 4 total-beats)))))

(defn tuplet
  "Returns the theoretical new note longeth when a note-length is made into a
   tuplet of n (i.e. for note-length 4, n 3, a quarter note triplet).
   
   e.g. quarter = 4, quarter note triplet = 6"
  [note-length n]
  (* n (/ note-length 2)))

(def base-note-lengths
  (merge
    ; american names
    {:double-whole          0.5
     :whole                 1
     :half                  2
     :quarter               4
     :eighth                8
     :sixteenth             16
     :thirty-second         32
     :sixty-fourth          64
     :hundred-twenty-eighth 128}
    ; british names
    {:breve                  0.5
     :semibreve              1
     :minim                  2
     :crotchet               4
     :quaver                 8
     :semiquaver             16
     :demisemiquaver         32
     :hemidemisemiquaver     64
     :semihemidemisemiquaver 128 }))

(def note-lengths
  (merge 
    base-note-lengths
    (into {} 
      (for [[k v] base-note-lengths]
        [(keyword (str "dotted-" (name k))) (dotted v 1)]))
    (into {} 
      (for [[k v] base-note-lengths]
        [(keyword (str "double-dotted-" (name k))) (dotted v 2)]))
    (into {} 
      (for [[k v] base-note-lengths]
        [(keyword (str "triple-dotted-" (name k))) (dotted v 3)]))
    (into {}
      (for [[k v] base-note-lengths]
        [(keyword (str (name k) "-triplet")) (tuplet v 3)]))
    (into {}
      (for [[k v] base-note-lengths]
        [(keyword (str (name k) "-quintuplet")) (tuplet v 5)]))
    (into {}
      (for [[k v] base-note-lengths]
        [(keyword (str (name k) "-septuplet")) (tuplet v 7)]))))

(defn note-length->duration [nl]
  (let [beats (/ 4 nl)
        beat-duration (* 1000 (/ 60 @*tempo*))] 
    (* beats beat-duration)))

(defn parse-duration [duration]
  (cond
    (number? duration)     duration
    (keyword? duration)    (do
                             (assert (contains? note-lengths duration)
                                     (str duration " is not a valid note length."))
                             (note-length->duration (note-lengths duration)))
    (sequential? duration) (apply + (map parse-duration duration))))

(defn- play-note*
  [osc-model {:keys [pitch duration volume] :as note-model}]
  (let [{:keys [osc-node gain-node clock] :as osc-impl} (osc* osc-model)]
    (freq osc-node (or pitch (:freq osc-model)))
    (gain-ramp gain-node (or volume (:gain osc-model) 1))

    (start-osc osc-impl)

    (when duration
      (let [duration-ms (parse-duration duration)]
        (c/set-timeout! clock #(stop-osc osc-impl) duration-ms)))))

(defn play-note 
  "Uses a one-off oscillator to play a note.

   Stops any currently playing oscillators belonging to `osc-model`.
   
   If duration is present, the oscillator is stopped after `duration` ms.

   Otherwise, the note rings out indefinitely, until the oscillator is stopped."
  [osc-model note-model]
  (stop-osc osc-model) 
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
            (let [duration-ms (parse-duration (or duration 0))] 
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
  (stop-osc osc-model) 
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
  (stop-osc osc-model)
  (play-chord* osc-model notes also-play-note))

(defn also-play-chord
  "Like `play-chord`, but does not stop any currently playing oscillators.
   
   Each note in `notes` does behave normally when played, i.e. if it has a 
   duration, it *will* stop after `duration` ms."
  [osc-model notes]
  (play-chord* osc-model notes also-play-note))

(comment "
  TODO: 
    - 'mute' functionality, stops oscs but also keeps track of their state so
      they can be restarted when 'unmuted'
    - a generic `play` function that will do the right thing, depending on the 
      types of the arguments
      e.g. multiple arguments are treated as a sequence of notes, a collection 
      of notes is treated as a chord
    - higher-level abstractions for pitch and duration, i.e. a G#5 half note at
      100 bpm
    - 'pause' functionality -- can add a `pause!` function to chronoid which
      will take a snapshot of the events currently scheduled (noting their
      deadlines relative to the current time) and unschedule them, and 
      `unpause!` and/or `toggle-pause!` to re-schedule the saved events 
      relative to the new current time. 

      For mantra, should use a combination of chronoid `pause!` and muting any
      currently active oscillators. When pause is toggled off, the oscillators
      should be unmuted and the clock un-paused.
")

