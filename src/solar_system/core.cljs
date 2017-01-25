(ns solar-system.core
  (:require [reagent.core :as reagent :refer [atom]]
            [physics.vector :as vector]
            [physics.star :as star]
            [physics.position :as position]
            [clojure.pprint :refer [pprint]])
  (:refer-clojure :exclude (vector)))

;;https://github.com/unclebob/clojureOrbit

(enable-console-print!)

(println "This text is printed from src/solar-system/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload


(def UNIT 0.1)
(def SOLAR-SYSTEM-SIZE 1000)
(def CENTER (position/make (/ SOLAR-SYSTEM-SIZE 2.0) (/ SOLAR-SYSTEM-SIZE 2.0)))

(defn random-velocity [p sun]
  (let [sp (:position sun)
        sd (position/distance p sp)
        v (Math/sqrt (/ 1 sd))
        direction (vector/rotate90 (vector/unit (vector/subtract p sp)))]
    (vector/scale direction (+ (rand 0.01) (* v 13.5 3)))))

(defn random-position [sun-position]
  (let [r (+ (rand 300) 30)
        theta (rand (* 2 Math/PI))]
    (position/add sun-position (position/make (* r (Math/cos theta)) (* r (Math/sin theta))))))

(defn random-star [sun n]
  (let [sp (:position sun)
        p (random-position sp)]
    (star/make p (rand 0.2) (random-velocity p sun) (vector/make) (str "r" n))))

(defn create-world []
  (let [v0 (vector/make)
        sun (star/make CENTER 1500 (vector/make 0 0) v0 "sun")]
    (loop [world [sun] n 20]
      (if (zero? n)
        world
        (recur (conj world (random-star sun n)) (dec n))))))

(defonce app-state (atom {:text "我的太阳系!"
                          :my-world (create-world)}))

(defonce orbit-history (atom []))


(defn main-svg []
  [:svg {:view-box [0 0 SOLAR-SYSTEM-SIZE SOLAR-SYSTEM-SIZE]
         :width 500
         :height 500}])

(defn all-stars []
  (for [star (:my-world @app-state)]
   [:circle {:r 5
             :cx (get-in star [:position :x])
             :cy (get-in star [:position :y])}]))

(defn all-history []
  (for [orbit-point @orbit-history]
   [:circle {:r 2
             :cx (get-in orbit-point [:x])
             :cy (get-in orbit-point [:y])}]))

(defn body []
  ; (into
  ;     (main-svg)
  ;     (all-stars))
  (-> (main-svg)
      (into (all-stars))))
      ; (into (all-history))))


(defn solar []
  [:center
    [:h1 (:text @app-state)]
    [body]])

(reagent/render-component [solar]
                          (. js/document (getElementById "app")))

(defn run-loop []
  (let [new-world (vec (second (star/update-all (:my-world @app-state))))
        orbit-point (map #(get-in % [:position]) new-world)
        history (into @orbit-history orbit-point)]
    (swap! app-state assoc-in [:my-world] new-world)
    (reset! orbit-history history)))

(defonce time-updater (js/setInterval run-loop
                       20))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ; (prn "-------" (:my-world @app-state))

  ; (pprint @app-state)
  ; (pprint (vec (second (star/update-all (:my-world @app-state)))))
  ; (pprint @orbit-history))
