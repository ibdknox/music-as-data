(ns music-as-data.track
  (:use music-as-data.core)
  (:gen-class)
  (:import java.util.TimerTask 
           java.util.Timer
           java.lang.Runnable
           java.util.concurrent.TimeUnit
           java.util.concurrent.Executors))


(def global-tempo (atom 60))
(def smallest-note 32)
(def *tracks* (atom []))

(defrecord Track [note pattern active])

(defn set-tempo [tempo]
  (swap! global-tempo (fn [n] tempo)))

(defn beat-length [] 
  (let [ms 60000]
    (/ ms @global-tempo)))

(defn tick-length []
    (int (/ (beat-length) smallest-note)))

(defn dur-to-tick [dur]
  "converts the beat duration into a tick count"
  (* dur smallest-note))

(defn set-tick [track tick]
  (let [note (:note track)]
    (assoc track :note 
           (assoc note :tick tick))))

(defn next-note [track]
  (-> track
    (assoc :note (first (:pattern track)))
    (assoc :pattern (rest (:pattern track)))))


(defn track-tick [track]
  (let [note (:note track)
        tick (if-not (:tick note)
               (dur-to-tick (:dur note))
               (:tick note))
        new-tick (dec tick)
        updated-track (set-tick track new-tick)]
    (when-not (= tick (:tick note))
      (play note))
    (if (= 0 new-tick) 
      (next-note updated-track)
      updated-track)))

(defn contains-notes [track]
  (let [tick (:tick (:note track))
        empty-pattern (empty? (:pattern track))]
    (or (not empty-pattern)
        (or (nil? tick) (> tick 1)))))

(defn on-tick [] 
  (when (seq @*tracks*)
    (count @*tracks*)
    (swap! *tracks* #(map track-tick (filter contains-notes %)))))

(defn track-from-pattern [pattern]
  (Track. (first pattern) (rest pattern) true))

(defn add-track [pattern]
  (swap! *tracks* conj (track-from-pattern pattern))
  nil)

(extend-protocol playable
  clojure.lang.ISeq
  (play [this] (add-track (pattern this))))

(def tick-loop (atom nil))

(defn start-tick [] 
  (swap! tick-loop (fn [x] (future
                             (loop []
                               (do
                                 (on-tick)
                                 (Thread/sleep (tick-length))
                                 (recur))
                               )))))

(defn stop-tick []
  (when (future? @tick-loop)
    (future-cancel @tick-loop)
    (swap! tick-loop (fn [x] nil))))

(def *timer* (. Executors newScheduledThreadPool 1))
(def *timer-task* ((bound-fn [] (proxy [Runnable] []
                    (run []
                         (on-tick)
                         )))))

(defn init-timer [] 
  (. *timer* (scheduleAtFixedRate *timer-task* (long 0) (long (tick-length)) (. TimeUnit MILLISECONDS))))

;; track
;; { :tick
;;   :note
;;   :pattern
;;   :tempo
;;   :timer }
