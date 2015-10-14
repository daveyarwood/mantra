# CHANGELOG

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
