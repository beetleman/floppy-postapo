(ns floppy-postapo.core
  (:require [play-cljs.core :as p]
            [goog.events :as events]
            [mount.core :as mount]
            [floppy-postapo.audio :as audio]
            [floppy-postapo.screen :as screen]
            [floppy-postapo.game :as g])
  (:require-macros [mount.core :refer [defstate]]))

(enable-console-print!)

;; If we are on the title screen a mouse click takes us to the next screen,
;; otherwise we minus the ship's y position to make it jump.
(defn event-handler []
  (let [game @g/game
        state @g/state
        gme (p/get-screen game)]
    (cond
      (= gme screen/title) (p/set-screen game screen/main)
      (= gme screen/main)  (swap! state update-in [:ship-v] #(- 12)))))

(defstate game-events
  :start (events/listen js/window "mousedown" event-handler)
  :stop (events/unlisten js/window "mousedown" event-handler))

(defn start []
  (mount/start)
  (doto @g/game
    (p/start)
    (p/set-screen screen/title)))

(start)


(defn on-js-reload [])
