(ns clj-tech-talk.prod
  (:require [clj-tech-talk.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
