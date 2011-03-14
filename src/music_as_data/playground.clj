(ns music-as-data.playground
  (:import [ddf.minim Minim]
           [ddf.minim.signals SquareWave]
           [ddf.minim AudioInput]
           [ddf.minim AudioOutput]
           [ddf.minim.signals SineWave])
  ;;(:use [music-as-data.signalsnotes])
  (:use [rosado.processing]
		[rosado.processing.applet]))

(defn setup []
  "Runs once."
  (def *minim* (Minim. *applet*))
  (def *line-out* (.getLineOut *minim*)))

(defn draw []
  (background-float 0)
  (stroke 255))

(defapplet main :title "Music as Data"
  :setup setup :draw draw :size [200 200])

(declare extract-note)

(defprotocol playable
  "determines how a musical element should be turned into audio"
  (play [this] "play the musical element"))

(defrecord Note [freq])

(defrecord Tone [wait dur note]
  playable
  (play [this] 
        (when-not (= (:note this) "_")
          (.playNote *line-out* (float (:wait this)) (float (:dur this)) (:note this)))))

(defrecord Multi-tone [wait dur notes]
  playable
  (play [this] 
        (doseq [n (:notes this)]
          (let [note (extract-note n)]
            (play (Tone. (:wait this) (:dur this) note))))))

(defrecord Sound-sequence [sounds]
  playable
  (play [this]
        (doseq [n (:sounds this)]
          (play n))))

(defrecord Sample [wait dur wav])
(defrecord Multi-sample [wait dur wavs])

(def A4-freq 440)
(def half-step-freq 1.059463)

(defn note-adjust [n]
  (let [adj (:adj n)]
    (cond
      (nil? adj) 0
      (= \b adj) -1
      (= \# adj) 1)))

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

(defn char-to-int [c]
  (Integer/parseInt (str c)))

(defn parse-note [n]
  (let [fields (if (> (count n) 2)
                 [:note :adj :scale]
                 [:note :scale])]
    (zipmap fields n)))

(defn extract-note [n]
  (cond
    (map? n) (:note n)
    (string? n) n
    (number? n) (float n)
    :else nil))

(defn note-diff [n1 n2]
  "The difference between two notes in half steps"
  (let [notes (map parse-note [n1 n2])
        adjust (reduce + (map note-adjust notes))
        scale-diff (* -12 (reduce - (map #(char-to-int (get % :scale 0)) notes)))
        tone-diff (* -1 (reduce - (map #(get scale-notes (:note %)) notes)))] 
    (+ adjust scale-diff tone-diff)))


(defn note [n]
  (if-let [note (extract-note n)]
    (Tone. 0 1 note)))

(defn chord [& notes]
  (Multi-tone. 0 1 notes))

(def a4 (Tone. 0 1 "A4"))
(def b4 (Tone. 0 1 "B4"))
(def c4 (Tone. 0 1 "C4"))
(def d4 (Tone. 0 1 "D4"))
(def e4 (Tone. 0 1 "E4"))
(def f4 (Tone. 0 1 "F4"))
(def g4 (Tone. 0 1 "G4"))
(def _ (Tone. 0 1 "_"))

(defn sub-pattern [pattern duration]
  (let [ndur (double (/ duration (count pattern)))]
    (map (fn [n]
           (when (vector? n)
             (sub-pattern n ndur))
             (assoc n :dur ndur))
         pattern)))

(defn flatten-pattern [p] 
  ;; [a b [c d] e]
  ;; [a1 b1 c.5 d.5 e1]
  (flatten (map (fn [n]
                  (if 
                    (vector? n) (sub-pattern n 1)
                    n)) 
                p)))

(defn adjust-wait [p]
  (let [wait (atom 0)]
    (map (fn [cur]
           (let [adjusted (assoc cur :wait @wait)]
             (swap! wait (partial + (:dur cur)))
             adjusted))
         p)))

(defn pattern [& groups]
  (Sound-sequence. (adjust-wait (flatten (map flatten-pattern groups)))))

;;(defsample kick "KickDrums1/kickdrum6.wav")
;;(defsample kick2 "KickDrums4/kickdrum154.wav")
;;(defsample snare "SnareDrums1/snaredrum2.wav")
;;(defsample snare2 "DistortedSnares2/distortedsnare52.wav")
;;(defsample hihat "HiHats1/hihat2.wav")

(run main)

;;(measures [a b c d] [a (extend b) [_ _ _ d]])
;;(play (pattern [a4 [c4 g4] (chord c4 f4 g4) [c4 g4 c4 g4]]
;;               [a4 d4 a4 g4]))
