(ns mantra.note.pitch
  (:require [music-theory.pitch :refer (note->hz)]))

(defn parse-pitch
  "If given a number (e.g. 440), returns the number, which will be interpreted
   as a frequency in Hz.

   If given something else (e.g. a string or keyword like \"C#5\" or :C#5),
   returns the result of calling `music-theory.pitch/note->hz` on it to get the
   frequency in Hz."
  [x]
  (if (number? x)
    x
    (note->hz x)))
