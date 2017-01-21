(ns solar-system.core
  (:require [reagent.core :as reagent :refer [atom]]
            [physics.vector :as vector]
            [physics.object :as object]
            [physics.position :as position])
  (:refer-clojure :exclude (vector)))

;;https://github.com/unclebob/clojureOrbit

(enable-console-print!)

(println "This text is printed from src/solar-system/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "我的太阳系!"
                          :position 0}))

(def UNIT 0.001)
(def SOLAR-SYSTEM-SIZE 10)

(defn create-world []
  (let [v0 (vector/make)
        sun (object/make center 1500 (vector/make 0 0) v0 "sun")]
    (loop [world [sun] n 400]
      (if (zero? n)
        world
        (recur (conj world (random-object sun n)) (dec n))))))

(defonce time-updater (js/setInterval
                       #(swap! app-state assoc-in [:position] (+ (:position @app-state) UNIT)) 16))






(defn body []
  [:svg {:view-box [0 0 SOLAR-SYSTEM-SIZE SOLAR-SYSTEM-SIZE]
         :width 500
         :height 500}
    [:circle {:r 1
              :cx (:position @app-state)
              :cy (:position @app-state)}]])


(defn solar []
  [:center
    [:h1 (:text @app-state)]
    [body]])

(reagent/render-component [solar]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (prn "-------" @app-state)
  (prn (create-world)))
