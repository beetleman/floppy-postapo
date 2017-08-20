(ns floppy-postapo.image
  (:require [play-cljs.core :as p]
            [floppy-postapo.game :refer [game]])
  (:require-macros [mount.core :refer [defstate]]))

(def splash "img/splash.png")
(def sky "img/sky.png")
(def land "img/land.png")
(def ship "img/spaceship.png")
(def pipe "img/pipe.png")
(def pipedown "img/pipedwn.png")

(defstate images
  :start (doto @game
           (p/load-image splash)
           (p/load-image sky)
           (p/load-image land)
           (p/load-image ship)
           (p/load-image pipe)
           (p/load-image pipedown)))
