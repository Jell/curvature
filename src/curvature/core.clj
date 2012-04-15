(ns curvature.core
  (:use quil.core)
  (:use [clojure.string :only [split split-lines replace]]))

(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader))

(defn setup []
  (smooth)                          ;;Turn on anti-aliasing
  (frame-rate 1)                    ;;Set framerate to 1 FPS
  (background 200))                 ;;Set the background colour to
                                    ;;  a nice shade of grey.
(def fetcher (agent [[0]]))

(defn draw []
  (stroke 0)      ;;Set the stroke colour to a random grey
  (stroke-weight 2) ;;Set the stroke thickness randomly
  (fill 255)         ;;Set the fill colour to a random grey
  (background 200)
  (doseq [[d i] (map list @fetcher (range))]
    (stroke (* 20 i))
    (doseq [l (partition 4 2
                         (interleave (range)
                                     (take-last (width)
                                                (map #(- (height) (/ % 100)) d))))]
      (apply line l))))

(defn parse-line [line]
  (map #(Float/parseFloat %) (split (second (split (replace line "None" "0") #"\|")) #",")))

(defn parse [raw-data]
  (map parse-line raw-data))

(defn fetch-url
  "returns lines from url"
  [address]
  (let [url (URL. address)]
    (with-open [stream (. url (openStream))]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
        (doall (line-seq buf))))))

(def data-url "http://graphite.domain:1234/render/?target=some.value&from=-24hours&rawData=true")

(defn fetch-data [&_]
  (parse (fetch-url data-url)))

(send fetcher fetch-data)

(.start (Thread. (fn []
                   (. Thread sleep 1000)
                   (send fetcher fetch-data)
                   (prn "updated")
                   (recur))))

(defsketch example ;;Define a new sketch named example
  :title "Some fun with graphite" ;;Set the title of the sketch
  :setup setup                     ;;Specify the setup fn
  :draw draw                       ;;Specify the draw fn
  :size [323 200]) ;;You struggle to beat the golden ratio
