(ns music-as-data.core
  (:import [ddf.minim Minim]
           [ddf.minim AudioOutput]))

(def *minim* (atom nil))
(def *line-out* (atom nil))

(declare extract-note)

(defprotocol playable
  "determines how a musical element should be turned into audio"
  (play [this] "play the musical element"))

(defrecord Note [freq])

(defrecord Tone [wait dur note]
  playable
  (play [this] 
        (when-not (= (:note this) "_")
          (.playNote @*line-out* (float (:wait this)) (float (:dur this)) (:note this)))))

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


(defn extract-note [n]
  (cond
    (map? n) (:note n)
    (string? n) n
    (number? n) (float n)
    :else nil))

(defn note [n]
  (if-let [note (extract-note n)]
    (Tone. 0 1 note)))

(defn chord [& notes]
  (Multi-tone. 0 1 notes))

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
