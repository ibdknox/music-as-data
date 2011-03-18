(ns music-as-data.notes
  (:import music-as-data.core.Tone)
  (:use music-as-data.utils))

(def A4-freq 440)
(def half-step-freq 1.059463)

(def octaves (range 1 9))

(def all-notes ["A" "Ab" "A#"
                "B" "Bb"
                "C" "C#"
                "D" "Db" "D#"
                "E" "Eb" 
                "F" "F#"
                "G" "Gb" "G#"])

(def scale-notes {\A 1 
                  ;;A# 2 
                  \B 3
                  \C 4 
                  ;;C# 5 
                  \D 6 
                  ;;D# 7 
                  \E 8
                  \F 9
                  ;;F# 10
                  \G 11
                  ;;G# 12
                  }) 

(defmacro create-notes []
  (cons 'do
        (for [octave octaves
                note all-notes]
          (let [tone (str note octave)]
            `(def ~(symbol (.toLowerCase (str tone))) (Tone. 0 1 ~(str tone)))))))


(def _ (Tone. 0 1 "_"))

(defn parse-note [n]
  (let [fields (if (> (count n) 2)
                 [:note :adj :scale]
                 [:note :scale])]
    (zipmap fields n)))

(defn note-adjust [n]
  (let [adj (:adj n)]
    (cond
      (nil? adj) 0
      (= \b adj) -1
      (= \# adj) 1)))

(defn note-diff [n1 n2]
  "The difference between two notes in half steps"
  (let [notes (map parse-note [n1 n2])
        adjust (reduce + (map note-adjust notes))
        scale-diff (* -12 (reduce - (map #(char-to-int (get % :scale 0)) notes)))
        tone-diff (* -1 (reduce - (map #(get scale-notes (:note %)) notes)))] 
    (+ adjust scale-diff tone-diff)))
