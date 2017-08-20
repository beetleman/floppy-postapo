(ns floppy-postapo.utils
  (:require [play-cljs.core :as p]))

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

(defn add-pipe-to-state [state]
  (swap! state update-in [:pipes]
         (fn [pipes]
           (apply conj (filter
                        (fn [pipe]
                          (< 0 (get-in pipe [1 :x]))) pipes)
                  (pipe-gen)))))
