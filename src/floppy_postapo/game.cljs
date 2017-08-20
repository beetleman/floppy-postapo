(ns floppy-postapo.game
  (:require [play-cljs.core :as p])
  (:require-macros [mount.core :refer [defstate]]))

(def initial-state {:timeoutid 0
                    :ship-p    100
                    :ship-v    0
                    :ship-a    1
                    :pipes     []})

(defn reset-state [state]
  (let [new-state (assoc initial-state :timeoutid (:timeoutid @state))]
    (reset! state new-state)))

(defonce game* (p/create-game 500 500))

(defstate game :start game*)

(defstate state
  :start (atom initial-state)
  :stop (reset-state @state))
