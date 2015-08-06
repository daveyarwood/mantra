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
(def sq (osc :type :square))
```

This produces a simple map that serves as a blueprint for making sound with this type of oscillator. Mantra's API functions all deal with this type of map, which we call the *oscillator model*.

For more options to the `osc` function, see the docstring for that function.

### The note model

Mantra also provides an abstraction for musical notes. A note is represented as a map containing the following keys:

* `:pitch` -- the frequency of the note, in Hz

* `:duration` (optional) -- the duration of the note, in milliseconds. When omitted, the note will sustain until it is stopped explicitly.

Mantra uses [chronoid](http://github.com/daveyarwood/chronoid) for accurate timing of notes, leveraging the accuracy of the Web Audio API's clock, which runs on a separate thread.

### Playing notes

There are a handful of functions available for playing notes, depending on the particular behavior that you want. These functions all take an oscillator model as the first argument, and some form of a note model (for `play-notes` and `play-chord`, it is a collection of note models) as the second argument.

#### `play-note`

```clojure
(play-note sq {:pitch 440 :duration 1000})
```

This sounds our oscillator for 1 second at 440 Hz.

One thing to note about this function is that any notes that the oscillator might be playing already will be stopped; in other words, this function tells the oscillator to drop whatever it's doing and play this note.

```clojure
(play-note sq {:pitch 440})

(js/window.setTimeout #(stop-osc sq) 1000)
```

*Docs under construction... hold please*

## License

Copyright © 2015 Dave Yarwood

Distributed under the Eclipse Public License version 1.0.

