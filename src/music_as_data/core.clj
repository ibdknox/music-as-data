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

(defn- playSeq [this]
  (doseq [n this]
    (play n)))

(extend-protocol playable
  clojure.lang.PersistentVector
  (play [this] (playSeq this))
  clojure.lang.LazySeq
  (play [this] (playSeq this)))

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

(defn adjust-wait [p initial]
  (let [wait (atom initial)]
    (map (fn [cur]
           (let [adjusted (assoc cur :wait @wait)]
             (swap! wait (partial + (:dur cur)))
             adjusted))
         p)))

(defn pattern [& groups]
  (vec (adjust-wait (flatten (map flatten-pattern groups)) 0)))

(defn combine
  ([patterns]
    (reduce (fn [notes1 notes2] 
              (let [lastnote (last notes1)]
                (concat 
                  notes1
                  (adjust-wait notes2 (+ (:wait lastnote)
                                         (:dur lastnote))))))
            patterns)))

(defn looping [times pattern]
  (let [looped (repeat pattern)]
    (combine (take times looped))))
