(ns brickbuilder.builder
  (:require [brickbuilder.bricks :as bricks]))

(defn create-workbench [size]
  (into {} (for [x (range size) y (range size)] [[x y] nil])))


(defmulti insert-tile (fn [_ _ {:keys [group]}] group))

(defmulti remove-tile (fn [_ _ {:keys [group]}] group))

(declare get-neighbors move-right put-tile rewire)

(defmethod insert-tile :default [workbench position tile]
  (let [neighbors (get-neighbors workbench position)
        tile-in-place (workbench position)]
    (-> workbench
        (move-right tile-in-place)
        (put-tile position tile)
        (rewire position))))

(defmethod insert-tile :flow [workbench position tile]
  )

(defn move-right [workbench {:keys [position] :as tile}]
  (if tile
    (let [[x y] position
          right-position [(inc x) y]
          right-tile (workbench right-position)]
      (-> workbench
          (dissoc )))
    workbench))

(defn put-tile [workbench position tile]
  )

(defn rewire [workbench position]
  (let [tile (workbench position)
        neighbors (get-neighbors workbench position)]))
