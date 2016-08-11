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

