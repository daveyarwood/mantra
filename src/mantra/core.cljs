(ns mantra.core)

(def ^:dynamic *audio-context*
  (let [constructor (or js/window.AudioContext
                        js/window.webkitAudioContext)]
    (constructor.)))

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
   they default to 440 Hz and 1.0 (full volume), respectively."
  [& {:keys [type freq gain] :as osc-map}]
  (let [type (name (or type :sine))]
    (if-not (contains? #{"sine" "square" "sawtooth" "triangle"} type)
      (throw (js/Error. (str type " is not a valid oscillator type.")))
      (assoc osc-map :type type
                     :id   (str (gensym type))))))

(defn- osc*
  "Creates a one-off oscillator based on a map, hooks it up to a gain node, 
   and hooks the gain node up to the destination of the AudioContext.

   Returns a map including the oscillator and gain nodes and the ID of the
   oscillator model used as a blueprint."
  [{:keys [id type freq]}]
  (let [osc  (.createOscillator *audio-context*)
        gain (.createGain *audio-context*)]
    (set! (.-type osc) (or type "sine"))
    (set! (.-value (.-frequency osc)) (or freq 440))
    (set! (.-value (.-gain gain)) 0)
    (.connect osc gain)
    (.connect gain (.-destination *audio-context*))
    {:osc-node  osc
     :gain-node gain
     :model-id  id}))

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
  (let [time (.-currentTime *audio-context*)]
    (.linearRampToValueAtTime (.-gain gain-node) level (+ time 0.1))))

(defn- silence [gain-node]
  (gain gain-node 0))

(defn- silence-ramp [gain-node]
  (gain-ramp gain-node 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare start-osc stop-osc)

(defn play-note 
  "Uses a one-off oscillator to play a note. 
   
   If duration is present, the oscillator is stopped after `duration` ms.

   Otherwise, the note rings out indefinitely, until the oscillator is stopped."
  [osc-model {:keys [pitch duration volume] :as note-model}]

  ; stop any currently playing oscillators for this osc-model 
  (stop-osc osc-model) 

  (let [{:keys [osc-node gain-node] :as osc-impl} (osc* osc-model)]
    (start-osc osc-impl)

    (freq osc-node (or pitch (:freq osc-model)))

    (gain-ramp gain-node (or volume (:gain osc-model) 1))

    (when duration
      (js/setTimeout #(stop-osc osc-impl) duration))))

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
    (let [{:keys [osc-node gain-node]} osc]
      (silence-ramp gain-node)
      (js/setTimeout #(do
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

(comment "
  TODO: 
    - make time more accurate by not just using setTimeout
        ref:
          http://www.html5rocks.com/en/tutorials/audio/scheduling
          https://github.com/cwilso/metronome/blob/master/js/metronome.js
    - play-notes and play-chord will also stop all notes
    - there should also be an also-play-note, which does the same thing as play-note, but doesn't stop any currently playing nodes
    - ditto for also-play-chord, also-play-notes 
    - 'mute' functionality, stops oscs but also keeps track of their state so
      they can be restarted when 'unmuted'
")

