(ns floppy-postapo.core
  (:require [play-cljs.core :as p]
            [goog.events :as events]
            [floppy-postapo.audio :as audio]))

(enable-console-print!)

(defonce game (p/create-game 500 500))
(defonce state (atom {:timeoutid 0
                      :ship-p    100
                      :ship-v    0
                      :ship-a    1
                      :pipes     []}))

(doto game
  (p/load-image "img/splash.png")
  (p/load-image "img/sky.png")
  (p/load-image "img/land.png")
  (p/load-image "img/spaceship.png")
  (p/load-image "img/pipe.png")
  (p/load-image "img/pipedwn.png"))

(declare title-screen)
(declare main-screen)

(defn calc-p [p v a]
  (let [t 0.25]
    (+ (* 0.5 t t a) (* t v) p)))

(defn calc-v [v a]
  (let [t 0.25]
    (+ (* t a) v)))

(defn move-ship [{:keys [ship-p ship-v ship-a] :as state}]
  (assoc state
    :ship-p (calc-p ship-p ship-v ship-a)
    :ship-v (calc-v ship-v ship-a)))

;If we are on the title screen a mouse click takes us to the next screen,
;otherwise we minus the ship's y position to make it jump.
(events/listen js/window "mousedown"
               (fn [_]
                 (let [gme (p/get-screen game)]
                   (cond
                     (= gme title-screen) (p/set-screen game main-screen)
                     (= gme main-screen)  (swap! state update-in [:ship-v] #(- 12))))))

;Top and bottom pipes are generated together as the gap between them should
;always be the same.
(defn pipe-gen []
  (let [rnd (rand 250)]
    [[:image {:name   "img/pipedwn.png"
              :width  50
              :height 400
              :x      550
              :y      (+ -400 rnd)}]
     [:image {:name   "img/pipe.png"
              :width  50
              :height 400
              :x      550
              :y      (+ 200 rnd)}]]))


;;http://stackoverflow.com/questions/23302698/java-check-if-two-rectangles-overlap-at-any-point
(defn collision-detection [images [_ {:keys [x y width height] :as ship}]]
  (let [diags         (map
                       (fn [[_ {:keys [x y width height] :as image}]]
                         {:x1 x
                          :y1 y
                          :x2 (+ x width)
                          :y2 (+ y height)})
                       images)
        overlap-check (fn [{:keys [x1 y1 x2 y2]}]
                        (let [shipx1 x shipy1 y shipx2 (+ x 60) shipy2 (+ y 60)]
                          (cond
                            (< shipx2 x1) false
                            (> shipx1 x2) false
                            (> shipy1 y2) false
                            (> y1 shipy2) false
                            :overlapping  true)))
        overlaps      (map overlap-check diags)]
    (some #(= true %) overlaps)))

(defn add-pipe-to-state []
  (swap! state update-in [:pipes]
         (fn [pipes]
           (apply conj (filter
                        (fn [pipe]
                          (< 0 (get-in pipe [1 :x]))) pipes)
                  (pipe-gen)))))

(def main-screen
  (reify p/Screen

    (on-show [this]
      ;Every four seconds we add two new pipes to a filtered list of the old pipes,
      ;where we remove pipes that have gone off the screen to the left.
      ;We also need to record the id of our call to setInterval so we can
      ;destroy it when we leave this screen.
      (add-pipe-to-state)
      (.play @audio/main)
      (swap! state update-in [:timeoutid]
             (fn [_] (js/setInterval
                      add-pipe-to-state
                       5000))))

    (on-hide [this]
      (.pause @audio/main)
      (js/clearInterval (:timeoutid @state)))

    (on-render [this]
      (let [{:keys [ship-p pipe pipes]} @state
            ship-img [:image {:name "img/spaceship.png" :width 60 :height 60 :x 200 :y ship-p}]]

        ;If the ship hits the ground or a pipe, return to the title screen and
        ;reset its position.
        (when (or (< 400 ship-p) (collision-detection pipes ship-img))
          (do
            (swap! state update-in [:pipes] (fn [_] []))
            (swap! state update-in [:ship-p] (fn [_] 0))
            (p/set-screen game title-screen)))

        ; Make the ship fall!
        (swap! state move-ship)

        ; Move all of our pipes to the left, to the left.
        (swap! state update-in [:pipes] (fn [pipes] (map
                                                      (fn [pipe]
                                                        (update-in pipe [1 :x] dec))
                                                      pipes)))

        (p/render game
                  [[:image {:name "img/sky.png" :width 500 :height 500 :x 0 :y 0}]
                   [:image {:name "img/land.png" :width 500 :height 100 :x 0 :y 450}]
                   ship-img])

        (p/render game pipes)))))

(def title-screen
  (reify p/Screen
    (on-show [this]
      (.play @audio/splash))
    (on-hide [this]
      (js/setTimeout #(.pause @audio/splash)))
    (on-render [this]
      (p/render game
                [[:image {:name "img/sky.png" :width 500 :height 500 :x 0 :y 0}]
                 [:image {:name "img/splash.png" :width 300 :height 300 :x 100 :y 100}]
                 [:image {:name "img/land.png" :width 500 :height 100 :x 0 :y 450}]]))))

(doto game
  (p/start)
  (p/set-screen title-screen))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
