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
