(ns music-as-data.core
  (:import [ddf.minim Minim]
           [ddf.minim AudioOutput]))

(def *minim* (atom nil))
(def *line-out* (atom nil))

(declare extract-note)
(defn sample-path [wav]
  (str "samples/" wav))

(defprotocol playable
  "determines how a musical element should be turned into audio"
  (play [this] "play the musical element"))

(defrecord Tone [dur note]
  playable
  (play [this] 
        (when-not (= (:note this) "_")
          (.playNote @*line-out* (float 0) (float (:dur this)) (:note this)))))

(defrecord Multi-tone [dur notes]
  playable
  (play [this] 
        (doseq [n (:notes this)]
          (let [note (extract-note n)]
            (play (Tone. (:dur this) note))))))

(defrecord Sample [dur wav]
  playable
  (play [this]
        (.trigger (.loadSample @*minim* (sample-path wav)))))

(defn extract-note [n]
  (cond
    (map? n) (:note n)
    (string? n) n
    (number? n) (float n)
    :else nil))

(defn note [n]
  (if-let [note (extract-note n)]
    (Tone. 1 note)))

(defn chord [& notes]
  (Multi-tone. 1 notes))

(defn sample [wav]
  (Sample. 1 wav))

(defn sub-pattern [pattern duration]
  (let [ndur (double (/ duration (count pattern)))]
    (map (fn [n]
           (if (vector? n)
             (sub-pattern n ndur)
             (assoc n :dur ndur)))
         pattern)))

(defn flatten-pattern [p] 
  ;; [a b [c d] e]
  ;; [a1 b1 c.5 d.5 e1]
  (flatten (map (fn [n]
                  (if 
                    (vector? n) (sub-pattern n 1)
                    n)) 
                p)))

(defn pattern [& groups]
  (flatten (map flatten-pattern groups)))

(defn looping 
  ([pat] (cycle (pattern pat)))
  ([times pat] (flatten (repeat times (pattern pat)))))
