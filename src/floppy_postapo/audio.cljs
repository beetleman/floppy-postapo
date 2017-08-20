(ns floppy-postapo.audio
  (:require [mount.core :refer [defstate]]))


(defstate main :start (let [audio (js/Audio. "/audio/main.ogg")]
                        (set! (.-loop audio) js/true)
                        audio))

(defstate splash :start (let [audio (js/Audio. "/audio/splash.ogg")]
                        (set! (.-loop audio) js/true)
                        audio))
