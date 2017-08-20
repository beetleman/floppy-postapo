(ns floppy-postapo.screen
  (:require [play-cljs.core :as p]
            [floppy-postapo.image :as image]
            [floppy-postapo.audio :as audio]
            [floppy-postapo.utils :as utils]
            [floppy-postapo.game :as g]))

(declare main)
(declare title)


(def title
  (let [game @g/game]
    (reify p/Screen
      (on-show [this]
        (.play @audio/splash))
      (on-hide [this]
        (js/setTimeout #(.pause @audio/splash)))
      (on-render [this]
        (p/render game
                  [[:image {:name image/sky :width 500 :height 500 :x 0 :y 0}]
                   [:image {:name image/splash :width 300 :height 300 :x 100 :y 100}]
                   [:image {:name image/land :width 500 :height 100 :x 0 :y 450}]])))))

(def main
  (let [game @g/game
        state @g/state]
    (reify p/Screen

      (on-show [this]
        ;;Every four seconds we add two new pipes to a filtered list of the old pipes,
        ;;where we remove pipes that have gone off the screen to the left.
        ;;We also need to record the id of our call to setInterval so we can
        ;;destroy it when we leave this screen.
        (utils/add-pipe-to-state state)
        (.play @audio/main)
        (swap! state update-in [:timeoutid]
               (fn [_] (js/setInterval
                        #(utils/add-pipe-to-state state)
                        5000))))

      (on-hide [this]
        (.pause @audio/main)
        (js/clearInterval (:timeoutid @state)))

      (on-render [this]
        (let [{:keys [ship-p pipe pipes]} @state
              ship-image [:image {:name image/ship :width 60 :height 60 :x 200 :y ship-p}]]

          ;;If the ship hits the ground or a pipe, return to the title screen and
          ;;reset its position.
          (when (or (< 400 ship-p) (utils/collision-detection pipes ship-image))
            (do
              (g/reset-state state)
              (p/set-screen game title)))

          ;; Make the ship fall!
          (swap! state utils/move-ship)

          ;; Move all of our pipes to the left.
          (swap! state update-in [:pipes] (fn [pipes] (map
                                                       (fn [pipe]
                                                         (update-in pipe [1 :x] dec))
                                                       pipes)))

          (p/render game
                    [[:image {:name image/sky :width 500 :height 500 :x 0 :y 0}]
                     [:image {:name image/land :width 500 :height 100 :x 0 :y 450}]
                     ship-image])

          (p/render game pipes))))))
