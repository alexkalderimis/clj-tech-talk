(ns clj-tech-talk.core
    (:require-macros [clj-tech-talk.core :refer [defslides]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; Views

(defn reveal [n snippet]
  (when (= (session/get :reveal) n) snippet))

(defn revealing [& xs]
  [:ul
   (doall
    (for [[i [point example]] (map vector (range) xs)]
      ^{:key i}
      (reveal i [:li point example])))])

(defn slide* [n slide prv nxt total]
  [:div.slide 
   [:div.heading
    [:h1.title (get-in slide [:heading :title])]
    [:h2.subtitle (get-in slide [:heading :subtitle])]]
   (:content slide)
   (into [] (concat [:footer [:a.home {:href "#/"} "\u2302"]]
           (when prv [[:a.prev {:href (str "#/" (dec n))} "\u00ab " (get-in prv [:heading :title])]])
           (when nxt [[:a.next {:href (str "#/" (inc n))} (get-in nxt [:heading :title]) " \u00bb"]])))
   [:div.progress n " of " total]])

(def un-refactored-thrush
  (str
    "(defn process [xs]\n"
    "  (->> (range)\n"
    "       (map vector xs)\n"
    "       (filter (compose even? second))\n"
    "       (map first)\n"
    "       (partition-all 5)\n"
    "       (map (reduce *))\n"))
(def mid-refactor-thrush
  (str
    "(defn every-second [xs]\n"
    "  (->> (range)\n"
    "       (map vector xs)\n"
    "       (filter (compose even? second))\n"
    "       (map first)))\n"
    "(defn process [xs]\n"
    "  (->> (range)\n"
    "       (map vector xs)\n"
    "       (filter (compose even? second))\n"
    "       (map first)\n"
    "       (partition-all 5)\n"
    "       (map (reduce *))\n"))

(def refactored-thrush
  (str
    "(defn every-second [xs]\n"
    "  (->> (range)\n"
    "       (map vector xs)\n"
    "       (filter (compose even? second))\n"
    "       (map first)))\n"
    "(defn process [xs]\n"
    "  (->> (every-second xs)\n"
    "       (partition-all 5)\n"
    "       (map (reduce *))\n"))

(def refactored-with-new-step
  (str
    "(defn every-second [xs]\n"
    "  (->> (range)\n"
    "       (map vector xs)\n"
    "       (filter (compose even? second))\n"
    "       (map first)))\n"
    "(defn process [xs]\n"
    "  (->> (every-second xs)\n"
    "       (map transform)\n"
    "       (partition-all 5)\n"
    "       (map (reduce *))\n"))

(def slide-handlers (defslides

  {:heading {:title "Clojure and Mori"
             :subtitle "[ :immutability :composability :extensibility ]"}
   :content (list
              [:p "The triumph of design, or why a language
                   from the 50s is relevant to developers of today"]
              [:img.cover {:src "/img/x1.jpg"}]
              )}

  {:heading {:title "What is new?"
             :subtitle "Nothing! Clojure is just..."}
   :content (list
              (revealing
                ["A Lisp"
                 [:pre "(repeatedly 10 #(println \"The lambda calculus rocks\"))"]]
                ["On the JVM"
                 [:pre "(doto (KafkaClient.) (.setJob job) (.run))"]]
                ["With collection literals"
                 [:pre
                  "{:x 1, :y 2, :c 3}\n"
                  "[1 2 3]\n"
                  "#{:a :b :c}\n"]]
                ["And destructuring"
                 [:pre
                  "(defn [{age :age, [given family] :name}]\n"
                  "  (if (> age 18)\n"
                  "    (println \"All hail \" given \" of house \" family)\n"
                  "    (println \"Aren't you cute\")))"
                  ]]
                ["Built on persistent data-structures"
                 [:pre
                  "(let [a {:x 1}\n"
                  "      b (into a [[:y 2] [:z 3]])]\n"
                  "  (= a b)) ;; false"]]
                ["And type-classes"
                 [:pre "(instance Fooish ... todo)"]])
              [:img.cover {:src "/img/sputnik.jpg"}])
   }

  {:heading {:title "Then why care?"}
   :content (list
              (revealing
      ["Elegant"
       [:img {:src "http://imgs.xkcd.com/comics/lisp_cycles.png"}]]
      ["Multi-platform"
       [:div
        [:span.label "JVM"]
        [:span.label "android"]
        [:span.label "node.js"]
        [:span.label "Browser"]]]
      ["Powerful"
       [:div
        [:span.label "concurrency"]
        [:span.label "tooling"]
        [:span.label "DSLs"]
        [:span.label "community"]
       ]]
      ["Pragmatic"
        [:div
          [:i "generally:"]
          [:span.label [:strong "pure"] " but side-effects are simple"]
          [:span.label [:strong "immutable"] " but mutability is available"]
          [:span.label [:strong "functional"] " but objects are easy to use"]
        [:p
          "In short, it makes good things easy, without tieing your hands"]]])
              [:img.cover {:src "/img/747.jpg"}])}

  {:heading {:title "The case for immutability"}
   :content (list
    [:p.quote
      "The absence of limitations is the enemy of art."
      [:span.by "Orson Welles"]]
    [:p
      "Forcing the developer to think about mutation makes
      for cleaner, more readable, more maintainable, more reusable
      code."]
    [:p
      "Ubiquitous immutability enables surprisingly useful features, such
      as:"]
    [:div
      [:label "Composite keys"]
      [:pre
      "(def board { [1 3] :pawn, [7 7] :rook})\n"
      "(= :rook (board [7 7]))"]]
    [:div
      [:label "Multiple tests"]
      [:pre
      "(is (= [1 2 3] ((juxt :age :strength :luck) monster)))"]]
    [:p "But the biggest feature is increased ability to compose functions"])}

  {:heading {:title "Super Underscore"}
   :content (list
    [:p.quote
      "This is the Unix philosophy: Write programs that do one thing and do it well.
      Write programs to work together. Write programs to handle text streams, because
      that is a universal interface."
      [:span.by "Doug McIlroy"]]
    [:p
      "For program read " [:strong "function"] ", for streams read "
      [:strong "collection"] ", for work together read " [:strong "compose"]]
    [:pre ;; TODO need better example - honey-sql, this very code!
      (case (session/get :reveal)
        -1 un-refactored-thrush
        0 mid-refactor-thrush
        1 refactored-thrush
        refactored-with-new-step)])}

;; slide-5 - clojure's immutable collections
;; small consistent API conj, assoc, into, map, filter, mapcat, reduce, update-in
;; little things - maps fns of keys, keywords fns of maps
;; everything uses them => composable DSLs (SQL, HTTP, templating, etc etc)
;; start with maps, then move to records
;; open for extension: easy to add new collection,
;;                     easy to extend collections to other interfaces
;; -- quite simply the best designed API I've ever worked with.
;; (and then lead into Mori, Immutable)


;; slide-6 - you can have this too! (mostly)
;; mori and Immutable.js
;; the very same API as clojure - the same collections framework as cljs
;; react, angular, backbone - observing mutation much easier and cleaner
;; when it is on immutable structures.
;; downsides - new collection API - not (trivially) interoperable 

  {:heading {:title "Other approaches"}
   :content (list
              [:p "This is not the only language/platform that has sought to address
                   the challenges of mutability in a concurrent environment"]
              (revealing
               ["node.js" "Single thread of execution, async I/O"]
               ["Go" "Channels"]
               ["Erlang" "processes, messages"]
               ["Akka" "actors, messages"]
               ["Haskell" "STM"]
               ["Ruby, Python", "GIL"]
               ["Java" "Synchronisation"]))}

  {:heading {:title "About these slides"}
   :content (list
    [:p
      "These slides are built with clojure! They make use of:"]
    [:ul
      [:li "Leiningen"]
      [:li "Reagent"]
      [:li "Secretary"]
      [:li "Compojure"]]
    [:p
      "You can run this presentation by cloning the repository
      and running the build:"]
    [:pre.console
      "> hub clone alexkalderimis/clj-tech-talk\n"
      "> lein figwheel"])}))

(defn current-page []
  (let [slide (slide-handlers (session/get :current-page))]
    [:div {:on-click #(session/update-in! [:reveal] inc)}
          [slide]]))

(defn clear-reveal []
  (session/put! :reveal -1))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (clear-reveal)
  (session/put! :current-page 0))

(secretary/defroute revealed-slide "/:slide.:reveal" [slide reveal]
  (session/put! :reveal            (int reveal))
  (session/put! :current-page (dec (int slide))))

(secretary/defroute "/:slide" [slide]
  (clear-reveal)
  (session/put! :current-page (dec (int slide))))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn next-page []
  (clear-reveal)
  (session/update-in! [:current-page]
                      (comp (partial min (dec (count slide-handlers))) inc)))

(defn prev-page []
  (clear-reveal)
  (session/update-in! [:current-page]
                      (comp (partial max 0) dec)))


(defn hook-events []
  (js/console.log "binding to " EventType/KEYUP)
  (events/listen
    js/window
    "keyup" ;; EventType.KEYUP
    #(case (.-keyCode %)
       38 (session/update-in! [:reveal] (comp (partial max -1) dec))
       40 (session/update-in! [:reveal] inc)
       32 (session/update-in! [:reveal] inc)
       39 (next-page)
       37 (prev-page)
       nil)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page]
                  (.getElementById js/document "app")))

(defn init! []
  (session/put! :current-page 0)
  (hook-browser-navigation!)
  (hook-events)
  (mount-root))
