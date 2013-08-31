(ns mikera.singaviz.main
  (:use [clojure.core.matrix])
  (:require [mikera.image.core :as img])
  (:require [clojure.java.io :as io]))

(defonce data (read (io/reader "viz.data")))
