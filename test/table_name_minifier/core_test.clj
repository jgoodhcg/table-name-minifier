(ns table-name-minifier.core-test
  (:require [clojure.test :refer :all]
            [table-name-minifier.core :refer :all]))

(deftest handle-commands-tests
  (testing "collection without commands present returns the same collection"
    (let [input ["some", "table", "name"]]
      (is (= (handle-commands input) input))))

  (testing "collection with commands present returns collection without commands"
    (is (= (handle-commands ["some", "--verbose", "name"]) ["some", "name"])))
  )

(deftest process-special-words-tests
  (testing "percent returns pct"
    (is (= (process-special-words "percent") "pct")))

  (testing "street returns st"
    (is (= (process-special-words "state") "st")))

  (testing "pound returns lb"
    (is (= (process-special-words "pound") "lb")))

  (testing "not special word returns input"
    (is (= (process-special-words "potato") "potato")))
  )

(deftest minify-input-tests
  (testing "single word normal input minified by removing vowels"
    (is (= (minify-input ["testing"]) ["tstng"])))

  (testing "multiple word normal input minified by removing vowels"
    (is (= (minify-input ["testing", "normal"]) ["tstng", "nrml"])))
  )
