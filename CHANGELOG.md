# CHANGELOG

## 0.6.1 (2019-09-20)

* First update in over 3 years! :tada:

  I just made some internal improvements to [chronoid], which are included by
  proxy in this release of mantra which depends on the new version of chronoid.

  You can read about the improvements in chronoid
  [here][chronoid-changelog-0.2.0], if you're curious.

[chronoid-changelog-0.2.0]: https://github.com/daveyarwood/chronoid/blob/master/CHANGELOG.md#020-2019-09-20

## 0.6.0 (2016-06-26)

* Added the ability to express the pitch of a note as a string or keyword containing the note (letter) name and octave.

  Example usage:

  ```clojure
  (require '[mantra.core :as m])

  (def o (m/osc :triangle))

  (m/play-notes o [{:pitch "C#5" :duration :quarter}
                   {:pitch :Ab3  :duration :dotted-half}])
  ```

## 0.5.4 (2016-02-20)

* Fixed a bug in [chronoid] that was affecting mantra to some extent. mantra now
  uses chronoid 0.1.1, which contains the fix.

## 0.5.3 (2016-02-20)

* Fixed a Firefox-specific bug caused by setting the gain value of a gain node
  to 0 and then trying to use
  [`exponentialRampToValueAtTime`](https://developer.mozilla.org/en-US/docs/Web/API/AudioParam/exponentialRampToValueAtTime).
  Per Mozilla's documentation:

  > A value of 0.01 was used for the value to ramp down to in the last function
  > rather than 0, as an invalid or illegal string error is thrown if 0 is used
  > -- the value needs to be positive.

  [It turns
  out](http://stackoverflow.com/questions/29819382/how-does-the-audioparam-exponentialramptovalueattime-work)
  that this applies not only to the value you're ramping to, but also the value
  you're ramping *from*.

  We were initializing oscillators with a gain node gain value of 0, which led
  to this bug when trying to use the oscillator to play notes.

  This should now work properly in Firefox. I also verified that it works in
  Safari. It was already working in Chrome.

## 0.5.2 (2016-02-20)

* Added in a safeguard against setting the tempo to `nil`. Previously this was
  happening if you called `(set-tempo)` without a `bpm` argument. Now this will
  throw an error instead.

## 0.5.1 (2015-10-14)

* Allow note lengths to be provided in string form.

## 0.5.0 (2015-10-08)

* Added `-triplet`, `-quintuplet`, and `-septuplet` suffixes to available note
  lengths.

## 0.4.0 (2015-08-18)

* Note-length abstractions (quarter, dotted eighth, triple-dotted
  semihemidemiquaver, etc.)

---

## 0.3.1 (2015-08-10)

* Allow notes with no duration in the middle as arguments to `play-notes`.

* Allow users to pass in their own AudioContexts and chronoid clocks rather than
  creating one internally.

## 0.3.0 (2015-08-05)

* Expanded note/chord functions -- added `play-notes`, `also-play-note`,
  `also-play-notes`, `play-chord`, `also-play-chord`.

---

## 0.2.1 (2015-08-03)

* Use [chronoid] for more accurate timing.

## 0.2.0 (2015-07-19)

* API changes, internal improvements

---

## 0.1.0 (2015-07-14)

* Initial release -- can create oscillators, use them to make bleeps & bloops.

[chronoid]: http://github.com/daveyarwood/chronoid
