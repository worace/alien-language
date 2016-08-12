(ns alien-language.core
  (:require [clojure.string :refer [split]]
            [clojure.set :refer [intersection union difference]]))

(defn lines [file] (-> file slurp (split #"\n")))

(defn parse-int [string] (Integer/parseInt string))

(defn read-lexicon
  "Read the lexicon sample contained in an input file. If the file is
   validly constructed we actually don't need the initial line telling
   how many cases are included, so I discard that and then just read cases
   until the end of the file."
  [file]
  (loop [lines (->> file lines (drop 1)) ;; dont need the initial line with the count
         chunks []]
    (if (empty? lines)
      chunks
      (let [count (parse-int (first lines))
            segment (take count (drop 1 lines))]
        (recur (drop (inc count) lines)
               (conj chunks segment))))))

(defn ordering [segment]
  (let [positions (zipmap segment (iterate inc 0))
        ordering (sort-by positions (set segment))]
    {:status "EXACT" :output (apply str ordering)}))

(def seq->str (partial apply str))

(defn next-layers [words]
  "Produce the 'next layers' according to letter prefixes for
   a group of words. Basically ['ab' 'zb' 'zc'] -> [['b'] ['b' 'c']]"
  (->> words
       (filter #(> (count %) 1))
       (group-by first)
       (map last)
       (map #(map rest %))
       (map #(map seq->str %))))

(defn flatten-segments
  "A collection of words 'ab', 'zb', 'zc' potentially contains multiple
   'segments' of ordering information, but the letter relationships need
   to be grouped according to the prefix of letters that precede them.

   So given the words 'ab', 'zb', 'zc', the segments of ordering info we
   actually have to look at are ['a', 'z', 'z'], ['b'], ['b', 'c'].

   However the single-letter ['b'] segment doesn't give us any useful info,
   so this function will ignore it."
  [words]
  (println "flattening segments for " (count words) "... " (take 5 words))
  (if (or (= 1 (count words)) (empty? words))
    []
    (let [this-layer (map str (map first words))
          next-layers (next-layers words)]
      (concat [this-layer]
              (mapcat flatten-segments next-layers)))))

(def blank-rels {:gt #{} :lt #{}})

(defn updated-rels
  [letter rels so-far to-come]
  (merge-with union
              rels
              {:gt (disj (set so-far) letter)
               :lt (disj (set to-come) letter)}))

(defn order-relationships [segment]
  (loop [rels {}
         so-far []
         to-come segment]
    (println "building rels for segment: " to-come)
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

(defn merged-order-relationships [words]
  (->> words
       flatten-segments
       (map order-relationships)
       (reduce (fn [left right] (merge-with union left right)))))


(defn next-letter [observed-rels {preceding :gt following :lt} letters]
  (filter (fn [letter]
            (= (observed-rels letter)
               {:gt (disj preceding letter)
                :lt (disj following letter)}))
          letters))

(def any? (comp not empty?))

(defn contradictory?
  "Ordering for a letter is contradictory if it appears both before
   and after another letter -- i.e. if there is any intersection between the
   set of letters preceding it and the set following it."
  [rels]
  (->> rels
       (filter (fn [[letter {preceding :gt following :lt}]]
                 (any? (intersection preceding following))))
       (map first)
       set
       any?))

(defn ambiguous?
  "Ordering for a letter is ambiguous if we can't establish
   its position relative to every other letter in the collection.
   We can tell if this is the case by comparing the union of
   the letters before and after it against the set of letters (minus itself)."
  [rels letters]
  (any? (filter (fn [[letter {preceding :gt following :lt}]]
            (not (= (disj letters letter) (union preceding following))))
          rels)))

(defn build-order [rels letters]
  (println "BUILDING ORDER FOR" rels letters)
  (cond
    (contradictory? rels) {:status "INCONSISTENT" :output (apply str (sort (set letters)))}
    (ambiguous? rels letters) {:status "AMBIGUOUS" :output (apply str (sort (set letters)))}
    :else (loop [order []
                 letters (set letters)]
            (if (empty? letters)
              {:status "EXACT" :output (apply str order)}
              (let [next (next-letter rels {:gt (set order) :lt letters} letters)]
                (recur (conj order (first next))
                       (disj letters (first next))))))))

(defn letters [words]
  (->> words (reduce concat) (map str) set))

(defn ordering-for-case [words]
  (-> words
      merged-order-relationships
      (build-order (letters words)))
  )
;; CASES

;; ab
;; zb
;; zc

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

(defn -main [& args]
  (->> "/usr/share/dict/words"
      lines
      (map clojure.string/lower-case)
      (random-sample 0.02)
      println
      ;; ordering-for-case
      ;; println
      ))
