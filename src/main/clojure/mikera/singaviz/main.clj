(ns mikera.singaviz.main
  (:use [clojure.core.matrix])
  (:use [mikera.cljutils.error])
  (:require [clojure.edn :as edn])
  (:require [mikera.image.core :as img])
  (:require [mikera.image.colours :as col])
  (:require [clojure.java.io :as io])
  (:import [mikera.gui Frames JIcon])
  (:import [java.awt.image BufferedImage])
  (:import [java.awt.event WindowListener])
  (:import [javax.swing JComponent JLabel JPanel JFrame])
  (:import [java.awt Graphics])
  (:import [mikera.matrixx AMatrix]))

;; load the visualization data from the "data.viz" resource file
(defonce data 
  (mapv 
    #(coerce :vectorz %) ;; convert each frame to a vectorz matrix
    (with-open [in (java.io.PushbackReader. (io/reader (io/resource "viz.data")))] (edn/read in))))
(def PERIODS (count data))
(def GW (column-count (first data)))
(def GH (row-count (first data)))

(def ^BufferedImage outline (img/load-image "outline-singapore.png")) 

(defn component 
  "Creates a component as appropriate to visualise an object x" 
  (^JComponent [x]
    (cond 
      (instance? BufferedImage x) (JIcon. ^BufferedImage x)
      :else (error "Don't know how to visualize: " x))))

(def last-window (atom nil))

(defn show 
  "Shows a component in a new frame"
  ([com 
    & {:keys [^String title on-close]
       :as options
       :or {title nil}}]
  (let [com (component com)
        ^JFrame fr (Frames/display com (str title))
        new-frame? (if (not (identical? fr @last-window)) (do (reset! last-window fr) true) false)]
    (when (and on-close new-frame?)
      (.addWindowListener fr (proxy [WindowListener] []
                               (windowActivated [e])
                               (windowClosing [e]
                                 (on-close))
                               (windowDeactivated [e])
                               (windowDeiconified [e])
                               (windowIconified [e])
                               (windowOpened [e])
                               (windowClosed [e])))))))

;; define colour for each number of calls
(defn col ^long [^double val]
  (let [lval (* (Math/log10 (+ 1.0 val)) 0.9)]
    (cond 
    (<= lval 0.0) 0xFF000000
    (<= lval 1.0) (let [v (- lval 0.0)] (col/rgb 0.0 0.0 v))
    (<= lval 2.0) (let [v (- lval 1.0)] (col/rgb v 0.0 (- 1.0 v)))
    (<= lval 3.0) (let [v (- lval 2.0)] (col/rgb 1.0 v 0.0))
    (<= lval 4.0) (let [v (- lval 3.0)] (col/rgb 1.0 1.0 v))
    :else 0xFFFFFFFFF)))

;; create an image frame from a matrix
(defn city-image ^BufferedImage [^AMatrix data]
  (let [^BufferedImage bi (img/new-image GW GH)]
    (dotimes [y GH]
      (dotimes [x GW]
        (.setRGB bi (int x) (int y) (unchecked-int (col (.get data (int y) (int x)))))))
    bi)) 


(defn frame 
  "Renders a specific frame of the visualization"
  ([i]
    (let [bi (img/zoom 8.0 (city-image (data i)))
          g (.getGraphics bi)]
      (.drawImage g outline (int 0) (int 0) nil)
      bi)))

(def running (atom true))

(defn run []
  (reset! running true)
  (while @running 
    (dotimes [i PERIODS] 
      (when @running
        (show (frame i) :title "Mobile Activity in Singapore" :on-close #(reset! running false))
        (Thread/sleep 50))))) 
