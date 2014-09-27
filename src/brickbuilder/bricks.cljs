(ns brickbuilder.bricks)

(def tile-defs
  {:play {:type :play
          :group :action}
   :loop {:type :loop
          :group :flow}
   :motor-this-way {:type :motor-this-way
                    :group :action}
   :motor-that-way {:type :motor-that-way
                    :group :action}
   :motor-stop {:type :motor-stop
                :group :action}
   :wait-for {:type :wait-for
              :group :action}
   :tilt-forward {:type :tilt-forward
                  :group :sensor}
   :tilt-backward {:type :tilt-backward
                   :group :sensor}
   :distance-smaller-than {:type :distance-smaller-than
                           :group :sensor}
   :distance-greater-than {:type :distance-greater-than
                           :group :sensor}
   :numeric-input {:type :numeric-input
                   :group :numeric-input}})

(declare apply-logic)

(defn execute [tiles last-result {:keys [next] :as tile}]
  (when tile
    (let [result (apply-logic tiles last-result tile)]
      (if next
        (recur tiles result (tiles next))
        result))))

(defmulti apply-logic (fn [_ _ {:keys [type]}] type))

(defmethod apply-logic :default [tiles last-result tile])

(defmethod apply-logic :wait-for [tiles last-result
                                  {:keys [condition next-after-wait] :as tile}]
  (if (number? condition)
    (js/setTimeout (fn []
                     (execute tiles nil (tiles next-after-wait)))
                   (* 1000 condition))
    (if (execute tiles nil condition)
      (execute tiles nil (tiles next-after-wait))
      (js/setTimeout (fn [] (execute tiles nil tile)) 200)))
  nil)

(defmethod apply-logic :play [_ _ tile]
  (println "Play got pressed!"))

(defmethod apply-logic :loop [tiles _
                              {:keys [block condition next-after-loop] :as tile}]
  (if (execute tiles _ condition)
    (execute tiles nil (tiles next-after-loop))
    (execute tiles nil (tiles block))))

(defmethod apply-logic :motor-this-way [tiles _ {:keys [value api]}]
  (if value
    (let [power (execute tiles nil value)]
      ((:set-motor api) value))
    ((:set-motor api) (:max-motor-power api))))

(defmethod apply-logic :motor-that-way [tiles _ {:keys [value api]}]
  (if value
    (let [power (execute tiles nil value)]
      ((:set-motor api) (- value)))
    ((:set-motor api) (- (:max-motor-power api)))))

(defmethod apply-logic :motor-stop [_ _ {:keys [api]}]
  ((:set-motor api) 0))

(defmethod apply-logic :distance-smaller-than [tiles _ {:keys [value api]}]
  (if value
    (execute tiles (fn [n] (< (:distance api) n)) value)
    (< ((:distance api)) (:min-distance api))))

(defmethod apply-logic :distance-greater-than [tiles _ {:keys [value api]}]
  (if value
    (execute tiles (fn [n] (> (:distance api) n)) value)
    (> ((:distance api)) (:min-distance api))))

(defmethod apply-logic :tilt-forward [_ _ {:keys [api]}]
  (= ((:tilt api)) :forward))

(defmethod apply-logic :tilt-backward [_ _ {:keys [api]}]
  (= ((:tilt api)) :backward))

(defmethod apply-logic :numeric-input [tiles last-result {:keys [value] :as tile}]
  (if (fn? last-result)
    (last-result value)
    value))

(def test-tiles
  {1 {:type :play
      :group :action
      :next 2}
   2 {:type :wait-for
      :group :action
      :attached-to 1
      :next-after-wait 3
      :condition 5}
   3 {:type :motor-this-way
      :group :action
      :attached-to 2
      :next 4
      :api {:set-motor (fn [n] (println (str "Setting motor power to " n)))
            :max-motor-power 100}}
   4 {:type :loop
      :group :flow
      :attached-to 3
      :block 2}})

(defn test-run []
  (execute test-tiles nil (get test-tiles 1)))
