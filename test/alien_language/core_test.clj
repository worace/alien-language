(ns alien-language.core-test
  (:require [clojure.test :refer :all]
            [alien-language.core :refer :all]))

(deftest reads-an-input
  (let [cases (read-lexicon "./resources/alien_language_sample.in")]
    (is (= 7 (count cases)))
    (is (= [3 2 3 2 2 4 3]
           (map count cases)))
    (is (= ["z" "x" "y"]
           (first cases)))))

(deftest determines-order-for-exact-segment
  (is (= {:status "EXACT" :output "cba"}
         (ordering ["c" "b" "a"])))
  (is (= {:status "EXACT" :output "cba"}
         (ordering ["c" "b" "a" "a"]))))

(deftest updating-order-relationships-for-samples
  (is (= {:gt #{\a \b} :lt #{\d \e}}
         (updated-rels \c blank-rels [\a \b] [\d \e]))))

(deftest building-order-relationships-for-a-segment
  (is (= {"z" {:gt #{} :lt #{"x" "y"}}
          "x" {:gt #{"z"} :lt #{"y"}}
          "y" {:gt #{"x" "z"} :lt #{}}}
         (order-relationships ["z" "x" "y"]))))

(deftest picking-next-match-from-a-segment-based-on-relationships
  (let [rels {"z" {:gt #{} :lt #{"x" "y"}}
              "x" {:gt #{"z"} :lt #{"y"}}
              "y" {:gt #{"x" "z"} :lt #{}}}]
    (is (= ["z"]
           (next-letter rels
                        {:gt #{} :lt #{"z" "y" "x"}})))
    (is (= ["x"]
           (next-letter rels
                        {:gt #{"z"} :lt #{"y" "x"}})))
    (is (= ["y"]
           (next-letter rels
                        {:gt #{"z" "x"} :lt #{"y"}})))))

(deftest building-ordering-from-observed-order-rels
  (let [rels {"z" {:gt #{} :lt #{"x" "y"}}
              "x" {:gt #{"z"} :lt #{"y"}}
              "y" {:gt #{"x" "z"} :lt #{}}}]
    (is (= {:status "EXACT", :output "zxy"}
           (build-order rels #{"z" "x" "y"})))))

(deftest observing-contradictory-order-relationships
  ;; ["z" "x" "z"] -> Bad because:
  ;; X appears both before and after Z
  ;; or...
  ;; Z appears both before and after X
  (is (= {"z" {:gt #{"x"} :lt #{"x"}}
          "x" {:gt #{"z"} :lt #{"z"}}}
         (order-relationships ["z" "x" "z"]))))

(deftest detecting-contradictory-relationships-when-rebuilding-order
  (let [rels {"z" {:gt #{"x"} :lt #{"x"}}
              "x" {:gt #{"z"} :lt #{"z"}}}]
    (is (contradictory? rels ["z" "x" "z"]))))

(deftest detecting-contradictory-ordering-due-to-prefix-problems
  (is (contradictory? {} ["hulu" "hul"])))

(deftest overall-order-for-contradictory-output
  (let [rels {"z" {:gt #{"x"} :lt #{"x"}}
              "x" {:gt #{"z"} :lt #{"z"}}}]
    (is (= {:status "INCONSISTENT" :output "xz"}
           (build-order rels ["z" "x" "z"])))))

(deftest determining-substrings
  (is (substring? "piz" "pizza"))
  (is (not (substring? "pizza" "piz")))
  (is (not (substring? "dog" "pizza"))))

(deftest flattening-segments
  ;; Given words:
  ;; ab
  ;; zb
  ;; zc
  ;; the actual segments we need to consider are:
  ;; ["a" "z" "z"] ["b"] ["b" "c"]
  ;; also we can ignore the single-letter segment b/c
  ;; it doesnt give us any info
  (is (= [["a" "z" "z"] ["b" "c"]]
         (flatten-segments ["ab" "zb" "zc"])))
  (is (= [["a" "b" "c"]]
         (flatten-segments ["a" "b" "c"])))
  (is (= [["c" "h"]]
         (flatten-segments ["ce" "he"]))))

(def alphabet (map str (map char (range 97 123))))

(def larger-sample ["armrest" "bewail" "castling" "dabs" "enroll"
                    "flocking" "garbles" "hero" "injustices" "join" "kite"
                    "ladle" "manual" "neckline" "oceanic" "parachuting"
                    "quinine" "recklessly" "simmering" "tipped" "urban"
                    "vector" "wainscotted" "xerography" "yacking" "zen"])

(def mega-alphabet ["a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "a" "b" "b" "b" "b" "b" "b" "b" "b" "b" "b" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "c" "d" "d" "d" "d" "d" "d" "d" "d" "d" "d" "d" "d" "d" "d" "d" "e" "e" "e" "e" "e" "e" "e" "e" "e" "e" "e" "e" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "f" "g" "g" "g" "g" "g" "g" "g" "g" "g" "g" "h" "h" "h" "h" "h" "h" "h" "i" "i" "i" "i" "i" "i" "i" "i" "i" "i" "i" "i" "j" "j" "j" "j" "j" "j" "k" "k" "k" "k" "k" "l" "l" "l" "l" "l" "m" "m" "m" "m" "m" "m" "m" "m" "m" "m" "m" "n" "n" "n" "n" "n" "o" "o" "o" "o" "o" "o" "o" "o" "o" "o" "o" "o" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "p" "q" "q" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "r" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "s" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "t" "u" "u" "u" "u" "u" "u" "u" "u" "u" "u" "u" "v" "v" "v" "w" "w" "w" "w" "w" "w" "x" "x" "y" "y" "z"])

(deftest beefier-test
  ;; Able to build proper ordering given whole alphabet in order...
  (is (= {:status "EXACT" :output (apply str alphabet)}
         (ordering-for-case alphabet)))
  (is (= {:status "EXACT" :output (apply str alphabet)}
         (ordering-for-case alphabet)))

  ;; Able to build proper ordering for alphabet with repetitions
  (is (= alphabet (first (flatten-segments larger-sample))))
  (is (= {:status "EXACT" :output (apply str alphabet)}
         (ordering-for-case mega-alphabet)))

  ;; Able to build order for alphabetical list of words (1 for each letter)
  (is (= {:status "EXACT" :output (apply str alphabet)}
         (ordering-for-case larger-sample)))

  (let [tricky ["aa" "ab" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m"
                "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"]
        tricky ["aa" "ab" "b" "c"]]
    ;; (is (= (conj alphabet "a") (first (flatten-segments tricky))))
    ;; (println "~~~~~~~~~~~~~~~~~~~~")
    ;; (println (->> tricky
    ;;          merged-order-relationships
    ;;          (filter (fn [[letter {lt :lt gt :gt}]]
    ;;                    (= 25 (count (clojure.set/union lt gt)))))
    ;;          ))
    ;; (println "~~~~~~~~~~~~~~~~~~~~")
    ;; (println (merged-order-relationships tricky))
    #_(is (= {:status "EXACT" :output (apply str alphabet)}
           (ordering-for-case tricky)))
    (is (= [["a" "a" "b" "c"] ["a" "b"]]
           (flatten-segments tricky)))
    (is (= {"a" {:gt #{} :lt #{"b" "c"}}
            "b" {:gt #{"a"} :lt #{"c"}}
            "c" {:gt #{"a" "b"} :lt #{}}}
           (order-relationships (first (flatten-segments tricky)))))
    (is (= {"a" {:gt #{} :lt #{"b"}}
            "b" {:gt #{"a"} :lt #{}}}
           (order-relationships (last (flatten-segments tricky)))))

    (is (= {"a" {:gt #{} :lt #{"b" "c"}}
            "b" {:gt #{"a"} :lt #{"c"}}
            "c" {:gt #{"a" "b"} :lt #{}}}
           (merge-order-relationships [{"a" {:gt #{} :lt #{"b" "c"}}
                                        "b" {:gt #{"a"} :lt #{"c"}}
                                        "c" {:gt #{"a" "b"} :lt #{}}}
                                       {"a" {:gt #{} :lt #{"b"}}
                                        "b" {:gt #{"a"} :lt #{}}}])))

    (is (= {:status "EXACT" :output "abc"}
         (ordering-for-case tricky))))
  )

(deftest merging-order-relationships-for-multi-letter-words
  ;; Given words:
  ;; ab
  ;; zb
  ;; zc
  ;; We should see that a is before z
  ;; b is before c
  ;; to build these, first consider the 0-th letter slice:
  ;; ["a" "z" "z"]
  ;; Then consider the relationships under a:
  ;; ["b"]
  ;; Then consider the relationships under z:
  ;; ["b" "c"]
  (is (= {"z" {:gt #{"a"} :lt #{}}
          "a" {:gt #{} :lt #{"z"}}
          "c" {:gt #{"b"} :lt #{}}
          "b" {:gt #{} :lt #{"c"}}}
         (merged-order-relationships ["ab" "zb" "zc"]))))

(deftest identifying-ambiguous-rels
  (let [rels (merged-order-relationships ["ce" "he"])]
    (is (ambiguous? rels #{"c" "e" "h"}))
    (is (not (ambiguous? rels #{"c" "h"})))))

(deftest full-cycle-inferring-from-case
  (is (= {:status "INCONSISTENT" :output "xz"}
         (ordering-for-case ["z" "x" "z"])))
  (is (= {:status "EXACT" :output "zx"}
         (ordering-for-case ["z" "x"])))
  (is (= {:status "AMBIGUOUS" :output "ceh"}
         (ordering-for-case ["ce" "he"])))
  (is (= {:status "INCONSISTENT" :output "bcdz"}
         (ordering-for-case ["zc" "bc" "zd"])))
  (is (= {:status "AMBIGUOUS" :output "xyz"}
         (ordering-for-case ["xy" "xyz"])))
  (is (= {:status "INCONSISTENT" :output "hlu"}
         (ordering-for-case ["hulu" "hul"])))
  (is (= {:status "EXACT" :output "abc"}
         (ordering-for-case ["a" "a" "b" "c"])))
  (is (= {:status "EXACT" :output "ej"}
         (ordering-for-case ["e" "je" "jj"]))))

(deftest processing-whole-file
  (is (= (slurp "./resources/alien_language_sample.out")
         (infer-from-file "./resources/alien_language_sample.in"))))
