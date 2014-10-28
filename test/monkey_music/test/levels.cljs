(ns monkey-music.test.levels
  (:require-macros [cemerick.cljs.test :refer (deftest is)])
  (:require [cemerick.cljs.test]
            [monkey-music.levels :as levels]))

(deftest test-parse-layout
  (is (= [[:empty :monkey]
          [:song  :playlist]]
         (levels/parse-layout
           [" M"
            "sp"]
           {" " "empty"
            "s" "song"
            "p" "playlist"
            "M" "monkey"}))))

(deftest test-parse-layout-with-unknown-unit
  (is (thrown? js/Error
               (levels/parse-layout
                 ["? "
                  " M"]
                 {" " "empty"
                  "M" "monkey"}))))

(deftest test-parse
  (is (= {:layout [[:empty :monkey]
                   [:song  :album]]
          :pick-up-limit 3
          :turns 10}
         (levels/parse
           {"units" {" " "empty"
                     "M" "monkey"
                     "s" "song"
                     "a" "album"}
            "layout" [" M"
                      "sa"]
            "pickUpLimit" 3
            "turns" 10}))))