# Music notation and transformation in Clojure #


# How to use #
    lein repl

You are now in the playground. You can play single notes like so:
    (play a4)
    (play c#4)

You can play a chord using the chord function:
    (play (chord c4 f4 g4))

To define your own notes (by either the name as a string or the frequency) you can do:
    (def tuning-A (note 440))
    (play tuning-A)
    (def my-D4 (note "D4"))
    (play my-D4)

More interestingly, you can create patterns of notes like so:
    (pattern [a4 c4 f4 g4])
In this case every given note in the vector represents one beat, this can be much more complicated, however, by using sub vectors to subdivide beats:
    (def background (pattern [a4 [c4 f4 g4 c4] [f4 [c4 g4 g4 g4]] c4]))
    (play background)

Patterns can also have chords:
    (def phrase (pattern [ [a4 [c5 c5]] [(chord a4 g4) [g4 (chord g4 a4) (chord e4 g4) a4]] [[a4 e4] e5]]))))     
And all patterns can be looped:
    (play (looping 4 phrase))

Patterns are just vectors of notes adjusted to follow after eachother, so you can perform operations on them:
    (play (adjust-wait (reverse (pattern [a4 b4 c4])) 0))

Patterns can also be combined, meaning that the notes will play sequentially after one another:
    (combine (pattern [a4 b4]) (pattern [c4 d4]))

# Building #
    lein deps

# Authors #

Implementation by Chris Granger
Adapted from the idea by Jon Vlachoyiannis (http://jon.is.emotionull.com).
