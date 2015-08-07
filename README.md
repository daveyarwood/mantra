> Don't have negative thoughts. Remember your mantra.
>
> gsb?8A?,INwbg3AU4IUx0PhA
> 
> \- Faxanadu

# mantra

[![Clojars Project](http://clojars.org/mantra/latest-version.svg)](http://clojars.org/mantra)

A ClojureScript library designed to make it easy to create music using the HTML5 Web Audio API. Mantra is similar in motivation to [Hum](https://github.com/mathias/hum), but aims to abstract away the lower-level implementation details and provide a higher-level API.

## Usage

```clojure
(require '[mantra.core :as m])
```

### The oscillator model

Normally if you want to make sound using the Web Audio API, you have to initialize your browser's AudioContext, create an instance of an oscillator, and hook it up to a gain node. Mantra abstracts away this boilerplate for you.

You can create an oscillator using the `osc` function:

```clojure
(def sq (m/osc :type :square))
```

This produces a simple map that serves as a blueprint for making sound with this type of oscillator. Mantra's API functions all deal with this type of map, which we call the *oscillator model*.

For more options to the `osc` function, see the docstring for that function.

One important difference between Mantra's oscillator models and the Web Audio API's more low-level OscillatorNodes generated by the `AudioContext.createOscillator` function, is that Web Audio API oscillators are intended to be used in a one-off fashion; for each note you want to play, you must create a new oscillator node, connect it to a gain node, set the frequency and gain, start it, and then stop it. Mantra handles all of this for you.

### The note model

Mantra also provides an abstraction for musical notes. A note is represented as a map containing the following keys:

* `:pitch` -- the frequency of the note, in Hz

* `:duration` (optional) -- the duration of the note, in milliseconds. When omitted, the note will sustain until it is stopped explicitly.

Mantra uses [chronoid](http://github.com/daveyarwood/chronoid) for accurate timing of notes, leveraging the accuracy of the Web Audio API's clock, which runs on a separate thread.

### Playing notes

There are a handful of functions available for playing notes, depending on the particular behavior that you want. These functions all take an oscillator model as the first argument, and some form of a note model (for `play-notes` and `play-chord`, it is a collection of note models) as the second argument.

#### `play-note`

```clojure
(m/play-note sq {:pitch 440 :duration 1000})
```

This sounds our oscillator for 1 second at 440 Hz.

One thing to note about this function is that any notes that the oscillator might be playing already will be stopped; in other words, this function tells the oscillator to drop whatever it's doing and play this note.

```clojure
(m/play-note sq {:pitch 440})

(js/window.setTimeout #(m/stop-osc sq) 1000)
```

This will also sound the oscillator for 1 second, but it does so in a way that demonstrates another possible way to use `play-note`: if you give it a note model that does not include a duration, the note will sound indefinitely until you stop it. As you can see, you can stop the note with `stop-osc`, a function that stops any notes that an oscillator model might be playing at a given moment.

#### `also-play-note`

```clojure
(m/play-note sq {:pitch 440})

(js/window.setTimeout #(m/also-play-note sq {:pitch 660}) 1000)

(js/window.setTimeout #(m/stop-osc sq) 2000)
```

`also-play-note` does the same thing as `play-note`, but with one important difference: it does not stop any notes that the oscillator model might already be playing. In the example above, we sound a note at 440 Hz and let it ring out, then, 1 second later, we *also* sound a note at 660 Hz, without stopping the first note. 1 second later than that, we stop both notes by calling `stop-osc` on the oscillator model.

#### `play-notes`

```clojure
(m/play-notes sq [{:pitch 100 :duration 333}
                  {:pitch 200 :duration 333}
                  {:pitch 300 :duration 333}
                  {:pitch 400 :duration 1000}])
```

`play-notes` plays a sequence of notes, one after the other. Each note starts when the note before it ends.

Like `play-note`, any notes that the oscillator model may have already been playing when this function is called, will be stopped. 

```clojure
(m/play-notes sq [{:pitch 220 :duration 250}
                  {:duration 250}
                  {:pitch 220 :duration 250}
                  {:duration 250}
                  {:pitch 440}])
```

If you omit the `:pitch` key from any of the notes, the note becomes a *rest* -- when the oscillator reaches that point in the note sequence, it will pause for `:duration` milliseconds.

If you omit the `:duration` key from any of the notes, the note will sustain indefinitely (until it is stopped explicitly) and if there is a note following it in the sequence, it will start playing immediately. (This is probably only useful if you want the *last* note in a sequence to sustain indefinitely.)

#### `also-play-notes`

`also-play-notes` : `play-notes` :: `also-play-note` : `play-note`

#### `play-chord`

```clojure
(m/play-chord [{:pitch 220 :duration 2000}
               {:pitch 330 :duration 2000}
               {:pitch 440 :duration 2000}])
```

`play-chord` plays a collection of notes simultaneously.

Just like `play-note` and `play-notes`, `play-chord` will stop any already-playing notes belonging to the oscillator model.

#### `also-play-chord`

You can probably guess what this function does.

## License

Copyright © 2015 Dave Yarwood

Distributed under the Eclipse Public License version 1.0.

