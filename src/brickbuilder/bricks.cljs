(ns brickbuilder.bricks)

(enable-console-print!)

(def tile-defs
  {:play {:type :play
          :group :action
          :can-attach-to #{[:action :next]}
          :slots {:next :east}
          :attached-to nil
          :next nil}
   :loop {:type :loop
          :group :flow
          :can-attach-to #{[:action :next]}
          :slots {:next :east, :condition :south, :block :west}
          :attached-to nil
          :next nil}
   :motor-this-way {:type :motor-this-way
                    :group :action
                    :can-attach-to #{[:action :next]
                                     [:flow :next]}
                    :slots {:next :east, :value :south}
                    :value nil
                    :attached-to nil
                    :next nil}
   :motor-that-way {:type :motor-that-way
                    :group :action
                    :can-attach-to #{[:action :next]
                                     [:flow :next]}
                    :slots {:next :east, :value :south}
                    :value nil
                    :attached-to nil
                    :next nil}
   :motor-stop {:type :motor-stop
                :group :action
                :can-attach-to #{[:action :next]
                                 [:flow :next]}
                :slots {:next :east}
                :attached-to nil
                :next nil}
   :wait-for {:type :wait-for
              :group :action
              :can-attach-to #{[:action :next]
                               [:flow :next]}
              :slots {:next :east, :condition :south}
              :attached-to nil
              :next nil}
   :tilt-forward {:type :tilt-forward
                  :group :sensor
                  :can-attach-to #{[:action :condition]
                                   [:flow :condition]}
                  :slots {:value :south}
                  :attached-to nil}
   :tilt-backward {:type :tilt-backward
                   :group :sensor
                   :can-attach-to #{[:action :condition]
                                    [:flow :condition]}
                   :slots {:value :south}
                   :attached-to nil}
   :distance-smaller-than {:type :distance-smaller-than
                           :group :sensor
                           :can-attach-to #{[:action :condition]
                                            [:flow :condition]}
                           :slots {:value :south}
                           :attached-to nil
                           :value nil}
   :distance-greater-than {:type :distance-greater-than
                           :group :sensor
                           :can-attach-to #{[:action :condition]
                                            [:flow :condition]}
                           :slots {:value :south}
                           :attached-to nil
                           :value nil}
   :numeric-input {:type :numeric-input
                   :group :input
                   :can-attach-to #{[:sensor :value]
                                    [:action :condition]
                                    [:action :value]}
                   :slots {}
                   :value 0
                   :attached-to nil}})

(defn prepare-toolbox [api]
  (reduce-kv (fn [m k v] (assoc m k (assoc v :api api))) {} tile-defs))

(declare execute)

(defn get-tile [tiles tile-id]
  (tiles tile-id))

(defn execute-next [tiles result {:keys [next] :as tile}]
  (if next
    (execute tiles result (get-tile tiles next))
    result))

(defmulti execute (fn [_ _ {:keys [type]}] type))

(defmethod execute :default [tiles last-result tile]
  (execute-next tiles nil tile))

(defmethod execute :wait-for [tiles last-result
                              {:keys [condition] :as tile}]
  (if (number? condition)
    (js/setTimeout (fn []
                     (execute-next tiles nil tile))
                   (* 1000 condition))
    (if (execute tiles nil (get-tile tiles condition))
      (execute-next tiles nil tile)
      (js/setTimeout (fn [] (execute tiles nil tile)) 200)))
  nil)

(defmethod execute :play [tiles _ tile]
  (println "Play got pressed!")
  (execute-next tiles nil tile))

(defmethod execute :loop [tiles _
                          {:keys [block condition] :as tile}]
  (if (execute tiles nil (get-tile tiles condition))
    (execute-next tiles nil tile)
    (execute tiles nil (get-tile tiles block))))

(defmethod execute :motor-this-way [tiles _ {:keys [value api] :as tile}]
  (if value
    (let [power (execute tiles nil (get-tile tiles value))]
      ((:set-motor api) power))
    ((:set-motor api) (:max-motor-power api)))
  (execute-next tiles nil tile))

(defmethod execute :motor-that-way [tiles _ {:keys [value api] :as tile}]
  (if value
    (let [power (execute tiles nil (get-tile tiles value))]
      ((:set-motor api) (- power)))
    ((:set-motor api) (- (:max-motor-power api))))
  (execute-next tiles nil tile))

(defmethod execute :motor-stop [tiles _ {:keys [api] :as tile}]
  ((:set-motor api) 0)
  (execute-next tiles nil tile))

(defmethod execute :distance-smaller-than [tiles _ {:keys [value api] :as tile}]
  (if value
    (execute tiles (fn [n] (< (:distance api) n)) (get-tile tiles value))
    (< ((:distance api)) (:min-distance api))))

(defmethod execute :distance-greater-than [tiles _ {:keys [value api] :as tile}]
  (if value
    (execute tiles (fn [n] (> (:distance api) n)) (get-tile tiles value))
    (> ((:distance api)) (:min-distance api))))

(defmethod execute :tilt-forward [tiles _ {:keys [api]}]
  (= ((:tilt api)) :forward))

(defmethod execute :tilt-backward [tiles _ {:keys [api]}]
  (= ((:tilt api)) :backward))

(defmethod execute :numeric-input [tiles last-result {:keys [value] :as tile}]
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
      :next 3
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
