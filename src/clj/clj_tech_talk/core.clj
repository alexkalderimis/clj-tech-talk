(ns clj-tech-talk.core)

(defmacro defslides [& slides]
  (let [slides (into [] slides) ;; vector for indexed access
        total (count slides)]
    `(vector
      ~@(for [[i slide] (map-indexed vector slides)
            :let [n (inc i)
                  prv (when (> i 0) (slides (dec i)))
                  nxt (when (< n (count slides)) (slides n))]]
        `(fn [] (clj-tech-talk.core/slide* ~n ~slide ~prv ~nxt ~total))))))
