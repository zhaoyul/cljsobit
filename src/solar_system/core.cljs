(ns solar-system.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [physics.vector :as vector]
            [physics.star :as star]
            [physics.position :as position]
            [clojure.pprint :refer [pprint]]
            [cljs.core.async :refer [ <!]])
  (:refer-clojure :exclude (vector)))


(enable-console-print!)

(println "This text is printed from src/solar-system/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload




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
    (star/make p (rand 5) (random-velocity p sun) (vector/make) (str "r" n))))

(defn create-world []
  (let [v0 (vector/make)
        sun (star/make CENTER 2000 (vector/make 0 0) v0 "sun")]
    (loop [world [sun]
           n 100]
      (if (zero? n)
        world
        (recur (conj world (random-star sun n)) (dec n))))))

(defonce app-state (atom {:text "我的太阳系!"
                          :my-world (create-world)}))

(defonce orbit-history (atom []))

(defn reset-orbit []
  (reset! orbit-history []))


(defn main-svg []
  [:svg {:view-box [0 0 SOLAR-SYSTEM-SIZE SOLAR-SYSTEM-SIZE]
         :width 500
         :height 500}])

(defn redius-for-star [star]
  (let [redius-const 2]
    (* redius-const (Math/pow (get-in star [:mass])
                              (/ 1 3)))))


(defn all-stars []
  (for [star (:my-world @app-state)]
    [:circle {:r (redius-for-star star)
              :fill (if (clojure.string/includes?  (:name star) "sun")
                      "yellow"
                      "white")
              :cx (get-in star [:position :x])
              :cy (get-in star [:position :y])}]))


(defn all-history []
  (for [orbit-point @orbit-history]
    [:circle {:r 1
              :fill "white"
              :cx (get-in orbit-point [:x])
              :cy (get-in orbit-point [:y])}]))


(defn body []
  (-> (main-svg)
      (into (all-stars))
      #_(into (all-history))))

(defn play-audio []
  (.play (.getElementById js/document "play")))


(go-loop []
  (let [x (<! star/audio-chan)]
    (play-audio))
  (recur))

(defn solar []
  [:center
   [:h1 (:text @app-state)]
   [:audio {:id "play" :src "explosion.mp3"}]
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
                                      50))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  ; (prn "-------" (:my-world @app-state))

  ; (pprint @app-state)
  ; (pprint (vec (second (star/update-all (:my-world @app-state)))))
  ; (pprint @orbit-history))
