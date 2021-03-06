(ns mantra.osc
  (:require [mantra.time   :as t]
            [mantra.sound  :as s]
            [chronoid.core :as c]))

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
  [& {:keys [type context clock] :as osc-map}]
  (let [type    (name (or type :sine))
        context (or context @s/*audio-context* (s/create-audio-context))]
    (if-not (contains? #{"sine" "square" "sawtooth" "triangle"} type)
      (throw (js/Error. (str type " is not a valid oscillator type.")))
      (assoc osc-map :type    type
                     :id      (gensym type)
                     :context context
                     :clock   (or clock @t/*clock* (t/create-clock context))))))

(defn osc*
  "Creates a one-off oscillator based on a map, hooks it up to a gain node,
   and hooks the gain node up to the destination of the AudioContext.

   Returns a map including the oscillator and gain nodes and the ID of the
   oscillator model used as a blueprint."
  [{:keys [id type freq context clock]}]
  (let [osc  (.createOscillator context)
        gain (.createGain context)]
    (set! (.-type osc) (or type "sine"))
    (set! (.-value (.-frequency osc)) (or freq 440))
    (set! (.-value (.-gain gain)) 0.001)
    (.connect osc gain)
    (.connect gain (.-destination context))
    {:osc-node  osc
     :gain-node gain
     :model-id  id
     :context   context
     :clock     clock}))

(def
  ^{:doc
    "A set of currently active oscillators. Each oscillator is represented as a
     map containing:

     :osc-node  -- the oscillator node :gain-node -- the gain node it's
     connected to :model-id  -- the ID of the model that was used as a blueprint
     to create the oscillator"
    :dynamic true}
  *oscillators* (atom #{}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn freq [osc-node hz]
  (set! (.-value (.-frequency osc-node)) hz))

(defn gain [gain-node level]
  (set! (.-value (.-gain gain-node)) level))

(defn gain-ramp [gain-node level]
  (let [time (.-currentTime (.-context gain-node))]
    (.exponentialRampToValueAtTime (.-gain gain-node) level (+ time 0.1))))

(defn silence [gain-node]
  (gain gain-node 0.001))

(defn silence-ramp [gain-node]
  (gain-ramp gain-node 0.001))

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

