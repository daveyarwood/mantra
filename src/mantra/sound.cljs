(ns mantra.sound)

(def ^:dynamic *audio-context* (atom nil))

(defn create-audio-context []
  (let [constructor (or js/window.AudioContext
                        js/window.webkitAudioContext)]
    (reset! *audio-context* (constructor.))))

