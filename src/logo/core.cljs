(ns logo.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [cljsjs.matter]))

;; Investigate whether this can enable cljs-devtools for println
(enable-console-print!)

;; Help for logging 
(defn log [& args]
  (.log js/console args))

;;Physics Stuff
(def Bodies js/Matter.Bodies)
(def Engine js/Matter.Engine)

(defn addToWorld! [engine objs]
  (.add js/Matter.World (.-world engine) (clj->js (map :body objs)))  
  objs
)

(defmulti create-body :type)

(defmethod create-body :box [{:keys [x y w h options] :as box }]
  (assoc box :body (.rectangle Bodies x y w h (clj->js options)))
)

(defmethod create-body :circle [{:keys [x y radius options] :as circle }]
  (assoc circle :body (.circle Bodies x y radius (clj->js options)))
)

;;Quil Stuff
(def the-world  [
                 {:type :box :x 250 :y 450 :w 500 :h 100 :options {:isStatic true}}
                 {:type :box :x 200 :y 0 :w 10 :h 10 :options {:restitution 0.8}}
                 {:type :circle :x 200 :y 40 :radius 10 :options {:restitution 0.8}}
                 {:type :circle :x 200 :y 80 :radius 10 :options {:restitution 0.9}}
                 ]
  )

(defn setup []
  (q/frame-rate 60)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  (q/rect-mode :center)
  (let [engine (.create js/Matter.Engine)
        bodies (map create-body the-world)]
    (addToWorld! engine bodies)
    
    {:engine engine
     :bodies bodies
     :millis (q/millis)
     :deltaO -1
     })
    
)

(defn update-state [{:keys [engine bodies millis deltaO] :as state}]
  (let [curMillis (q/millis)
        deltaT (if (< deltaO 0) 16 (- curMillis millis))
        correction (if (< deltaO 0) 0 (/ deltaT deltaO))]
    (.update js/Matter.Engine engine deltaT correction)
    {:engine engine
     :bodies bodies
     :millis (q/millis)
     :deltaO deltaT
     }
  ))

(defn quil-draw [position angle draw-fn]
  (q/push-matrix)
  (q/translate (.-x position) (.-y position))
  (q/rotate angle)
  (draw-fn)
  (q/pop-matrix)
)

(defmulti draw-body :type)

(defmethod draw-body :box [{:keys [w h body]}]
  (let [position (.-position body)
        angle (.-angle body)
        ]
    (quil-draw position angle #(q/rect 0 0 w h))
    )
)

(defmethod draw-body :circle [{:keys [radius body]}]
  (let [position (.-position body)
        diameter (* 2 radius)
        ]
    (quil-draw position 0 #(q/ellipse 0 0 diameter diameter))
    )
)

(defn draw-state [{:keys [engine bodies millis] :as state}]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
                                        ; Set circle color.
  (q/fill 32 255 255)

  (doseq [body bodies] 
    (draw-body body)))

(q/defsketch logo
  :host "logo"
  :size [500 500]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
