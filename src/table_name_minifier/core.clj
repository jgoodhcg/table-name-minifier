(ns table-name-minifier.core
  (:require [clojure.string :as str])
  (:gen-class :main true))

(def verbose (atom false))

(defn command-help
  "Display the help screen"
  []
  (println "Usage: tnmin [--help] <command> [<args>]")
  ;; TODO this crashes the repl and also isn't represented in tests for same reason
  (System/exit 0))

(defn command-verbose
  "Enable verbose mode"
  []
  (reset! verbose true))

(def commands [{:name "--help" :function command-help}
               {:name "--verbose" :function command-verbose}])

(def abbreviations [{:word "percent" :abbreviation "pct"}
                    {:word "state" :abbreviation "st"}
                    {:word "pound" :abbreviation "lb"}])

(def default-max-length 32)

(defn remove-vowels
  "Remove vowels from the string"
  [input]
  (str/replace input #"[aeiou]" ""))

(defn minify-special-words
  [input]
  (->> input
       (map (fn [word]
              (->> abbreviations
                   ;; find and pass along an abbreviation if it exists
                   (some (fn [abbreviation-map]
                           (if (= (:word abbreviation-map) word)
                             (:abbreviation abbreviation-map))))
                   ;; if there wasn't one then % will be null and it will use the original word instead
                   (#(if (some? %) % word)))))))

(defn handle-commands
  "Handle special commands"
  [input]
  (doall ;; This makes sure that the side effects of commands (verbose atom setting) happen in order despite a lazy seq
   (->> input
        (map (fn [word]
               ;; find any commands
               (let [command (->> commands (some #(if (= (:name %) word) % nil)))]
                 (if (some? command)
                   ;; run them if they are there and replace that spot in the input vec with  nil
                   (do
                     ((:function command))
                     nil)
                   word))))
        ;; remove those substituted nils
        (remove nil?))))

(defn strip-separators
  "Remove word separators, turns string into a vector"
  [input]
  (str/split input #"[_|\s]"))

(defn reform-table-name
  "Reform table name"
  [input]
  (clojure.string/join "_" input))

(defn minify-input
  "Condense the input collection into a reduced collection"
  ([input]
   (minify-input input default-max-length)) ;; TODO this is multi arity example
  ([input max-length]
   (when @verbose (println "... abbreviating special words"))
   (let [first-pass (minify-special-words input)
         first-pass-count (->> first-pass reform-table-name count)]
     (if (> first-pass-count max-length)
       ;; If it is too long lets remove some vowels
       (do (when @verbose (println "... string still too long removing vowels"))
           (->> first-pass
                (map (fn [word]
                       ;; account for not removing any vowels for already abbreviated words
                       ;; TODO might want to add some sort of test for this case (there isn't an abbreviation with a vowel right now)
                       (let [abbrevs (->> abbreviations (map :abbreviation))]
                         (if (some? (->> abbrevs (some #(= word %))))
                           word
                           (remove-vowels word)))))))
       ;; Otherwise it is good and pass it along
       first-pass))))

(defn -main
  "Take user input and process"
  [input]
  (println
   (->> input
        strip-separators
        handle-commands
        minify-input ;; TODO added multi arity to keep this thread clean and backwards compatible (for tests)
                     ;; Otherwise it would be (#(minify-input % default-max-length))
        reform-table-name)))
