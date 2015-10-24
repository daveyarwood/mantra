(ns mantra.note.duration
  (:require [mantra.time :as t]))

(defn- dotted
  "Returns the theoretical new note length when a note-length is given the
   specified number of dots.

   e.g. given that quarter = 4, dotted quarter = 2-2/3"
  [note-length dots]
  (let [beats (/ 4 note-length)]
    (loop [total-beats beats, factor 0.5, dots dots]
      (if (pos? dots)
        (recur (+ total-beats (* beats factor)) (* factor 0.5) (dec dots))
        (/ 4 total-beats)))))

(defn- tuplet
  "Returns the theoretical new note longeth when a note-length is made into a
   tuplet of n (i.e. for note-length 4, n 3, a quarter note triplet).

   e.g. quarter = 4, quarter note triplet = 6"
  [note-length n]
  (* n (/ note-length 2)))

(def ^:private base-note-lengths
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

(def ^:private note-lengths
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

(defn- note-length->duration [nl]
  (let [beats (/ 4 nl)
        beat-duration (* 1000 (/ 60 @t/*tempo*))]
    (* beats beat-duration)))

(defn parse-duration [duration]
  (cond
    (number? duration)     duration
    (keyword? duration)    (do
                             (assert (contains? note-lengths duration)
                                     (str duration " is not a valid note length."))
                             (note-length->duration (note-lengths duration)))
    (string? duration)     (parse-duration (keyword duration))
    (sequential? duration) (apply + (map parse-duration duration))))

