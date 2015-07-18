(ns mantra.core)

(def ^:dynamic *audio-context*
  (let [constructor (or js/window.AudioContext
                        js/window.webkitAudioContext)]
    (constructor.)))

(def *active-oscs* (atom #{}))

(defn osc 
  "Models the state of an oscillator.
   
   e.g. (osc :type :square
             :freq 440
             :gain 0.5)
   
   This produces a map that is used to create a fresh oscillator every time
   one is needed. Because oscillators can only be started once, this
   provides an abstraction for a 'persistent oscillator' that can be reused 
   any number of times (but under the hood, it's just a blueprint for any
   number of oscillators)."
  [& {:keys [type freq gain] :as osc-map}]
  (let [type (name type)]
    (when-not (contains? #{"sine" "square" "sawtooth" "triangle"} type)
      (throw (js/Error. (str type " is not a valid oscillator type."))))
    (assoc osc-map :type type)))

(defn- osc*
  "Creates a one-off oscillator based on a map, hooks it up to a gain node, 
   and hooks the gain node up to the destination of the AudioContext.

   Returns both the oscillator and its gain node."
  [{:keys [type freq]}]
  (let [osc  (.createOscillator *audio-context*)
        gain (.createGain *audio-context*)]
    (set! (.-type osc) (or type :sine))
    (set! (.-value (.-frequency osc)) (or freq 440))
    (set! (.-value (.-gain gain)) 0)
    (.connect osc gain)
    (.connect gain (.-destination *audio-context*))
    {:osc-node  osc
     :gain-node gain}))

(defn- freq [osc-node hz]
  (set! (.-value (.-frequency osc-node)) hz))

(defn- gain [gain-node level]
  (set! (.-value (.-gain gain-node)) level))

(defn- gain-ramp [gain-node level]
  (let [time (.-currentTime *audio-context*)]
    (.linearRampToValueAtTime (.-gain gain-node) level (+ time 0.1))))

(defn- silence [gain-node]
  (gain gain-node 0))

(defn- silence-ramp [gain-node]
  (gain-ramp gain-node 0))

(declare stop-osc)
(defn play-note 
  "Uses a one-off oscillator to play a note. 
   
   If duration is present, the oscillator is stopped after duration ms, and the
   oscillator model is returned unmodified.

   Otherwise, the note rings out indefinitely, and the oscillator model map is
   updated to include the osc and gain nodes, allowing them to be stopped or
   modified later."
  [osc-model {:keys [pitch duration volume]}]
  ; if the osc-model has any currently playing oscillators, stop them 
  ; (does nothing if there are no currently playing oscillators)
  (stop-osc osc-model)

  (let [{:keys [osc-node gain-node] :as osc-impl} (osc* osc-model)]
    (.start osc-node 0)
    (swap! *active-oscs* conj osc-node)

    (when pitch 
      (freq osc-node pitch))

    (gain-ramp gain-node (or volume (:gain osc-model) 1))

    (if duration
      (do
        (js/setTimeout #(stop-osc osc-impl) duration)
        osc-model)
      (assoc osc-model :osc-nodes #{osc-impl}))))

(defn stop-osc
  "Silences and stops a currently playing oscillator.
   
   This fn can take either an osc-model or an osc-impl as an argument."
  [osc]
  (cond
    (and (contains? osc :osc-node) (contains? osc :gain-node))
    (let [{:keys [osc-node gain-node] :as osc-impl} osc]
      (silence-ramp gain-node)
      (js/setTimeout #(do
                        (.stop osc-node)
                        (swap! *active-oscs* disj osc-node)) 1000))
    
    (contains? osc :osc-nodes)
    (let [{:keys [osc-nodes] :as osc-model} osc]
      (doseq [osc-node osc-nodes]
        (stop-osc osc-node)))))

(comment "
  TODO: 
    - play-note should stop any currently playing nodes
    - (play-notes and play-chord will also do this)
    - there should also be an also-play-note, which does the same thing as play-note, but doesn't stop any currently playing nodes
    - ditto for also-play-chord, also-play-notes 
    - stop-all-oscs (will need to keep track of them in a top-level dynamic var, like *oscillators*)
    - make time more accurate by not just using setTimeout
        ref:
          http://www.html5rocks.com/en/tutorials/audio/scheduling
          https://github.com/cwilso/metronome/blob/master/js/metronome.js
")

