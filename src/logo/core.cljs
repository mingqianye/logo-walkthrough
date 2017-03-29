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


(defn create-box [x y w h options] 
  {:w w :h h :body (.rectangle Bodies x y w h (clj->js options))}
)

;;Quil Stuff

(defn setup []
  (q/frame-rate 60)
  ; Set color mode to HSB (HSV) instead of default RGB.
  (q/color-mode :hsb)
  (q/rect-mode :center)
  (let [ground (create-box 250 450 500 100 {:isStatic true})
        box (create-box 200 0 10 10 {:restitution 1})
        engine (.create Engine)
        bodies [ground box]
        ]
    (addToWorld! engine bodies)
    
    {:engine engine
     :bodies bodies
     :millis (q/millis)
     :deltaO -1
     }
    )
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

(defn draw-box [{:keys [w h body]}]
  (let [position (.-position body)
        angle (.-angle body)
        ]
  (q/push-matrix)
  (q/translate (.-x position) (.-y position))
  (q/rotate angle)
  (q/rect 0 0 w h)
  (q/pop-matrix)
  
    )
)

(defn draw-state [{:keys [engine bodies millis] :as state}]
  ; Clear the sketch by filling it with light-grey color.
  (q/background 240)
                                        ; Set circle color.
  (q/fill 32 255 255)

  (doseq [box bodies] 
    (draw-box box)))

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
