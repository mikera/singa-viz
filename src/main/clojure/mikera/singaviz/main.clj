(ns mikera.singaviz.main
  (:use [clojure.core.matrix])
  (:require [clojure.edn :as edn])
  (:require [mikera.image.core :as img])
  (:require [clojure.java.io :as io]))

;; load the visualization data from the "data.viz" resource file
(defonce data 
  (mapv 
    #(coerce :vectorz %) ;; convert each frame to a vectorz matrix
    (with-open [in (java.io.PushbackReader. (io/reader (io/resource "viz.data")))] (edn/read in))))
