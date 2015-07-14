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
   number of oscillators)."
  [& {:keys [type freq gain] :as osc-map}]
  (let [type (name type)]
    (when-not (contains? #{"sine" "square" "sawtooth" "triangle"} type)
      (throw (js/Error. (str type " is not a valid oscillator type."))))
    (assoc osc-map :type type)))

(defn osc*
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
    (.connect gain (.destination *audio-context*))
    {:osc  osc
     :gain gain}))

(defn freq [osc hz]
  (set! (.-value (.-frequency osc)) hz))

(defn gain [gain level]
  (set! (.-value (.-gain gain)) level))

(defn gain-ramp [gain level]
  (let [time (.currentTime *audio-context*)]
    (.linearRampToValueAtTime gain level (+ time 0.1))))

(defn silence [gain]
  (gain gain 0))

(defn silence-ramp [gain]
  (gain-ramp gain 0))

(defn play-note 
  "Uses a one-off oscillator to play a note. 
   
   If duration is present, the oscillator is stopped after duration ms.
   Otherwise, the note rings out indefinitely. 
   
   Returns the input oscillator model, with the new oscillator and gain nodes
   included so that the oscillator/gain can be stopped or modified later."
  [osc-model {:keys [pitch duration volume]}]
  (let [{:keys [osc gain]} (osc* osc-model)]
    (.start osc 0)

    (when pitch 
      (freq osc pitch))

    (gain-ramp gain (or volume (:gain osc-model) 1))

    (when duration
      (js/setTimeout #(silence-ramp osc) duration)
      (js/setTimeout #(.stop osc) (+ duration 1000)))

    (assoc osc-model :osc-node osc :gain-node gain)))

