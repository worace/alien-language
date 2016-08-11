(ns alien-language.core
  (:require [clojure.string :refer [split]]))

(defn lines [file] (-> file slurp (split #"\n")))

(defn parse-int [string] (Integer/parseInt string))

(defn read-lexicon [file]
  (loop [lines (->> file lines (drop 1)) ;; dont need the initial line with the count
         chunks []]
    (if (empty? lines)
      chunks
      (let [count (parse-int (first lines))
            segment (take count (drop 1 lines))]
        (recur (drop (inc count) lines)
               (conj chunks segment))))))

#_(reduce (fn [positions [letter position]]
            (update positions letter conj position))
          {}
          (map vector segment (range (count segment))))
(defn ordering [segment]
  (let [positions (zipmap segment (iterate inc 0))
        ordering (sort-by positions (set segment))]
    {:status "EXACT" :output (apply str ordering)}))
;; CASES

;; 1 - Exact
;; output all letters in the order established by the sample
;; eg ["a" "b" "c"]
;; 2 - Ambiguous
;; output the letters sorted by english
;; eg ["ce" "he"] => "ceh" (take all unique letters in the segment and sort them)
;; 3 - Inconsistent / Contradictory
;; output the letters sorted by english


;; 7
;; 3
;; z
;; x
;; y
;; 2
;; ce
;; he
;; 3
;; zc
;; bc
;; zd
;; 2
;; xy
;; xyz
;; 2
;; hulu
;; hul
;; 4
;; a
;; a
;; b
;; c
;; 3
;; e
;; je
;; jj
