(ns slides)


(defmacro defslides [& slides]
  `(list
    ~@(for [[i slide] (map-indexed vector slides)
          :let [n (inc i)]]
      `(fn [] (slide* ~n ~slide)))))


