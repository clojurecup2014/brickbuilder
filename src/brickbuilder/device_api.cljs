(ns brickbuilder.device-api)

(def max-motor-power 100)

(def max-distance 100)

(def min-distance 0)

(defn connected? []
  true)

(defn set-motor [power]
  (println (str "Setting motor power to " power)))

(defn distance []
  42)

(defn tilt []
  :backward)
