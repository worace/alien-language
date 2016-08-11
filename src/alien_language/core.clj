(ns alien-language.core
  (:require [clojure.string :refer [split]]
            [clojure.set :refer [intersection union difference]]))

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

(def blank-rels {:gt #{} :lt #{}})

(defn updated-rels [letter rels so-far to-come]
  (merge-with union
              rels
              {:gt (disj (set so-far) letter)
               :lt (disj (set to-come) letter)}))

(defn order-relationships [segment]
  (loop [rels {}
         so-far []
         to-come segment]
    (if (empty? to-come)
      rels
      (let [current-letter (first to-come)
            current-rels (get rels current-letter blank-rels)]
        (recur (assoc rels
                      current-letter
                      (updated-rels current-letter
                                    current-rels
                                    so-far
                                    to-come))
               (conj so-far current-letter)
               (rest to-come))))))


(defn next-letter [observed-rels {preceding :gt following :lt} letters]
  (filter (fn [letter]
            (= (observed-rels letter)
               {:gt (disj preceding letter)
                :lt (disj following letter)}))
          letters))

(defn determine-order [rels letters]
  (loop [order []
         letters (set letters)]
    (if (empty? letters)
      order
      (let [next (next-letter rels {:gt (set order) :lt letters} letters)]
        (recur (conj order (first next))
               (disj letters (first next)))))))

;; CASES

;; z
;; x
;; y

;; layer 1 --
;; z < x
;; x < y

;; produce the ordering of #{x y z}
;; that satisifies those constraints
;; {:z {:lt #{x y} :gt #{}}
;;  :x {:gt #{z} :lt #{y}}
;;  :y {:get #{x z} :lt #{}}
;; }
;; {:z {:lt x}}


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
