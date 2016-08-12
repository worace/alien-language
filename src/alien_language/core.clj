(ns alien-language.core
  (:require [clojure.string :refer [split join]]
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
  (if (or (= 1 (count words)) (empty? words))
    []
    (let [this-layer (map str (map first words))
          next-layers (next-layers words)]
      (concat [this-layer]
              (mapcat flatten-segments next-layers)))))

(def blank-rels {:preceding #{} :following #{}})

(defn updated-rels
  [letter rels so-far to-come]
  (merge-with union
              rels
              {:preceding (disj (set so-far) letter)
               :following (disj (set to-come) letter)}))

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

(defn merge-order-relationships
  "Combine the order relationships created from multiple letter segments.
   We want to retain all of the relationships for each letter, so we union
   the appropriate keys together.
   So one map of {b {:preceding #{a} :following #{c}}} and another of {b {:gt #{} #{d e}}}
   will produce a single map {b {:get #{a} :following #{c d e}}}"
  [rels-seq]
  (if (empty? rels-seq) {}
      (reduce (fn [rels-a rels-b]
                (merge-with (fn [letter-rels-a letter-rels-b]
                              (merge-with union letter-rels-a letter-rels-b))
                            rels-a rels-b))
              rels-seq)))

(defn merged-order-relationships
  "Compile all the order-relationship info we can out of a sequence of
   words.

   Do this by:
   1) producing all of possible the letter sequences out of the words
   2) building the order relationships for each of these
   3) merging these together"
  [words]
  (->> words
       flatten-segments
       (map order-relationships)
       merge-order-relationships))


(defn next-letter
  "Choose the next letter that comes in order. Here 'preceding'
   represents the letters we have ordered so far and 'following'
   represents all the remaining letters, so we're basically picking
   from the remaining which letter matches the current sequence
   of before and after."
  [observed-rels {preceding :preceding following :following}]
  (filter (fn [letter]
            (= (observed-rels letter)
               {:preceding (disj preceding letter)
                :following (disj following letter)}))
          following))

(defn determinate-order
  "Use the order relationship information we gathered to build
   the ordering for the provided collection of letters.

   The rels information provides for each letter the list of known
   letters that precede and follow it. So as we walk through the letters
   we can look at each point at

   a) the letters whose ordering we have established so far and
   b) the letters still to come

   and pick the remaining letter which matches that set of preceding
   and following letters."
  [rels letters]
  (loop [order []
         letters (set letters)]
    (if (empty? letters)
      {:status "EXACT" :output (apply str order)}
      (let [next (next-letter rels {:preceding (set order) :following letters})]
        (recur (conj order (first next))
               (disj letters (first next)))))))

(def any? (comp not empty?))

(defn substring? [string potential-parent]
  (and (> (count potential-parent) (count string))
       (= string (subs potential-parent 0 (count string)))))

(defn prefixes-out-of-order? [words]
  (loop [words-so-far #{}
         words words
         out-of-order false]
    (cond
      out-of-order true
      (empty? words) false
      :else (recur (conj words-so-far (first words))
                   (rest words)
                   (any? (filter (partial substring? (first words))
                                 words-so-far))))))

(defn contradictory?
  "Ordering for a letter is contradictory if it appears both before
   and after another letter -- i.e. if there is any intersection between the
   set of letters preceding it and the set following it."
  [rels words]
  (or (prefixes-out-of-order? words)
      (->> rels
           (filter (fn [[letter {preceding :preceding following :following}]]
                     (any? (intersection preceding following))))
           (map first)
           set
           any?)))

(defn ambiguous?
  "Ordering for a letter is ambiguous if we can't establish
   its position relative to every other letter in the collection.
   We can tell if this is the case by comparing the union of
   the letters before and after it against the set of letters (minus itself)."
  [rels letters]
  (or (empty? rels)
      (any? (filter (fn [[letter {preceding :preceding following :following}]]
                      (not (= (disj letters letter) (union preceding following))))
                    rels))))

(defn letters [words]
  (->> words (reduce concat) (map str) set))

(defn build-order [rels words]
  (let [letters (letters words)]
    (cond
      (contradictory? rels words) {:status "INCONSISTENT" :output (apply str (sort (set letters)))}
      (ambiguous? rels letters) {:status "AMBIGUOUS" :output (apply str (sort (set letters)))}
      :else (determinate-order rels letters))))

(defn ordering-for-case [words]
  (-> words
      merged-order-relationships
      (build-order words)))

(defn format-case-result [number case-info]
  (str "Case #" number \newline (:output case-info) \newline (:status case-info)))

(defn infer-from-file [file]
  (str (->> file
            read-lexicon
            (map ordering-for-case)
            (map format-case-result (iterate inc 1))
            (join \newline))
       \newline))

(defn select-simple-words [words]
  (filter (fn [w] (nil? (re-find #"[^a-z]" w))) words))

(defn -main [& args]
  (println "Attempting to order English using 5% sample from the dictionary...")
  (->> "/usr/share/dict/words"
      lines
      select-simple-words
      (random-sample 0.05)
      ordering-for-case
      println))
