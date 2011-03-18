(ns music-as-data.playground
  (:use [music-as-data.core]
        [music-as-data.notes]
        [music-as-data.applet]))

(create-notes)
(play (looping 4 (pattern [ [a4 [c5 c5]] [(chord a4 g4) [g4 (chord g4 a4) (chord e4 g4) a4]] [[a4 e4] e5]])))      

