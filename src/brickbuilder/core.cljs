(ns brickbuilder.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [brickbuilder.bricks :as bricks]))

(enable-console-print!)

(def app-state (atom {:text "Brick Builder"}))

(om/root
  (fn [app owner]
    (reify om/IRender
      (render [_]
              (dom/div nil
               (dom/h1 nil (:text app))
               (dom/p nil "Here will be an online version of the Brick Builder app - without actual HW integration. The final result should work as a Chrome app.")))))
  app-state
  {:target (. js/document (getElementById "app"))})
