(ns music-as-data.applet
  (:import [ddf.minim Minim]
           [ddf.minim AudioOutput])
  (:use [music-as-data.core]
        [rosado.processing]
		[rosado.processing.applet]))

(defn setup []
  "Runs once."
  (reset! *minim* (Minim. *applet*))
  (reset! *line-out* (.getLineOut @*minim*)))

(defn draw []
  (background-float 0)
  (stroke 255))

(defapplet main :title "Music as Data"
  :setup setup :draw draw :size [200 200])

(run main)
