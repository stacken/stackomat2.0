(ns ^:figwheel-no-load frontend.dev
  (:require [frontend.core :as core]
            [figwheel.client :as figwheel :include-macros true]))

(enable-console-print!)

(core/init!)
