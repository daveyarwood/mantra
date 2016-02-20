# CHANGELOG

## 0.5.4 (2/20/16)

* Fixed a bug in [chronoid](http://github.com/daveyarwood/chronoid) that was affecting mantra to some extent. mantra now uses chronoid 0.1.1, which contains the fix.

## 0.5.3 (2/20/16)

* Fixed a Firefox-specific bug caused by setting the gain value of a gain node to 0 and then trying to use [`exponentialRampToValueAtTime`](https://developer.mozilla.org/en-US/docs/Web/API/AudioParam/exponentialRampToValueAtTime). Per Mozilla's documentation:

  > A value of 0.01 was used for the value to ramp down to in the last function rather than 0, as an invalid or illegal string error is thrown if 0 is used -- the value needs to be positive.

  [It turns out](http://stackoverflow.com/questions/29819382/how-does-the-audioparam-exponentialramptovalueattime-work) that this applies not only to the value you're ramping to, but also the value you're ramping *from*.

  We were initializing oscillators with a gain node gain value of 0, which led to this bug when trying to use the oscillator to play notes.

  This should now work properly in Firefox. I also verified that it works in Safari. It was already working in Chrome.

## 0.5.2 (2/20/16)

* Added in a safeguard against setting the tempo to `nil`. Previously this was happening if you called `(set-tempo)` without a `bpm` argument. Now this will throw an error instead.

## 0.5.1 (10/14/15)

* Allow note lengths to be provided in string form.

## 0.5.0 (10/8/15)

* Added `-triplet`, `-quintuplet`, and `-septuplet` suffixes to available note lengths.

## 0.4.0 (8/18/15)

* Note-length abstractions (quarter, dotted eighth, triple-dotted semihemidemiquaver, etc.)

---

## 0.3.1 (8/10/15)

* Allow notes with no duration in the middle as arguments to `play-notes`.

* Allow users to pass in their own AudioContexts and chronoid clocks rather than creating one internally.

## 0.3.0 (8/5/15)

* Expanded note/chord functions -- added `play-notes`, `also-play-note`, `also-play-notes`, `play-chord`, `also-play-chord`.

---

## 0.2.1 (8/3/15)

* Use [chronoid](http://github.com/daveyarwood/chronoid) for more accurate timing.

## 0.2.0 (7/19/15)

* API changes, internal improvements

---

## 0.1.0 (7/14/15)

* Initial release -- can create oscillators, use them to make bleeps & bloops.
