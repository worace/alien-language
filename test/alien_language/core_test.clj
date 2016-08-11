(ns alien-language.core-test
  (:require [clojure.test :refer :all]
            [alien-language.core :refer :all]))

(deftest reads-an-input
  (let [segments (read-lexicon "./resources/alien_language_sample.in")]
    (is (= 7 (count segments)))
    (is (= [3 2 3 2 2 4 3]
           (map count segments)))
    (is (= ["z" "x" "y"]
           (first segments)))))

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
                        {:gt #{} :lt #{"z" "y" "x"}}
                        #{"z" "y" "x"})))
    (is (= ["x"]
           (next-letter rels
                        {:gt #{"z"} :lt #{"y" "x"}}
                        #{"y" "x"})))
    (is (= ["y"]
           (next-letter rels
                        {:gt #{"z" "x"} :lt #{"y"}}
                        #{"y"})))))

(deftest building-ordering-from-observed-order-rels
  (let [rels {"z" {:gt #{} :lt #{"x" "y"}}
              "x" {:gt #{"z"} :lt #{"y"}}
              "y" {:gt #{"x" "z"} :lt #{}}}]
    (is (= ["z" "x" "y"]
           (determine-order rels #{"z" "x" "y"})))))

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
    (is (= #{"z" "x"}
           (contradictions rels)))))

(deftest overall-order-for-contradictory-output
  (let [rels {"z" {:gt #{"x"} :lt #{"x"}}
              "x" {:gt #{"z"} :lt #{"z"}}}]
    (is (= {:status "INCONSISTENT" :output "xz"}
           (determine-order rels ["z" "x" "z"])))))

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

(deftest observing-relations-for-multi-letter-words-segments
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
         (order-relationships ["ab" "zb" "zc"]))))
