(ns monkey-music.core-test
  (:require-macros [cemerick.cljs.test :refer (deftest is testing)])
  (:require [cemerick.cljs.test :refer (test-ns)]
            [monkey-music.core :as c]
            [monkey-music.random :as r]))

(deftest test-move-to-empty
  (is (= {:layout [[::c/open-door ::c/monkey]]
          :original-layout [[::c/open-door ::c/empty]]
          :teams {"1" {:position [0 1]}}} 
         (c/run-command
           {:layout [[::c/monkey ::c/empty]]
            :original-layout [[::c/open-door ::c/empty]]
            :teams {"1" {:position [0 0]}}}
           {:team-name "1" :command-name "move" :direction :right}))))

(deftest test-value-of
  (is (= 1 (c/value-of ::c/song))))

(deftest test-move-to-user
  (is (= {:layout [[::c/monkey ::c/user]]
          :teams {"1" {:position [0 0]
                       :picked-up-items [::c/banana]
                       :score (+ (c/value-of ::c/song)
                                 (c/value-of ::c/album))}}} 
         (c/run-command
           {:layout [[::c/monkey ::c/user]]
            :teams {"1" {:position [0 0]
                         :picked-up-items [::c/banana ::c/song ::c/album]
                         :score 0}}}
           {:team-name "1" :command-name "move" :direction :right}))))

(deftest find-positions
  (is (= (c/find-positions
           [[::c/monkey ::c/empty ::c/song]
            [::c/empty ::c/monkey ::c/monkey]]
           isa? ::c/monkey))))

(deftest test-create-team
  (is (= (c/create-team [0 0])
         {:position [0 0]
          :buffs {}
          :picked-up-items []
          :score 0})))

(deftest test-create-teams
  (is (= (c/create-teams ["1" "2"] [[0 0] [1 1] [2 2]])
         {"1" {:position [0 0] :buffs {} :picked-up-items [] :score 0}
          "2" {:position [1 1] :buffs {} :picked-up-items [] :score 0}})))

(deftest test-create-game-state
  (is (= (with-redefs [r/create (constantly :mock)]
           (c/create-game-state
             ["1" "2"]
             {:layout [[::c/monkey ::c/empty ::c/monkey ::c/monkey]]
              :pick-up-limit 3
              :turns 10}))
         {:teams {"1" {:position [0 0] :buffs {} :picked-up-items [] :score 0}
                  "2" {:position [0 2] :buffs {} :picked-up-items [] :score 0}}
          :random :mock
          :pick-up-limit 3
          :remaining-turns 10
          :layout [[::c/monkey ::c/empty ::c/monkey ::c/empty]]
          :original-layout [[::c/monkey ::c/empty ::c/monkey ::c/empty]]})))

(deftest test-move-to-tunnel-entrance
  (is (= (c/run-command
           {:layout [[::c/tunnel-exit-1 ::c/monkey ::c/tunnel-entrance-1]]
            :original-layout [[::c/tunnel-exit-1 ::c/empty ::c/tunnel-entrance-1]]
            :teams {"1" {:position [0 1]}}}
           {:team-name "1" :command-name "move" :direction :right})
         {:layout [[::c/monkey ::c/empty ::c/tunnel-entrance-1]]
          :original-layout [[::c/tunnel-exit-1 ::c/empty ::c/tunnel-entrance-1]]
          :teams {"1" {:position [0 0]}}})))

(deftest test-tackle-monkey
  (is (= (with-redefs [r/weighted-selection! (constantly true)]
           (c/run-command
              {:layout [[::c/monkey ::c/monkey ::c/empty]]
               :original-layout [[::c/empty ::c/empty ::c/empty]]
               :random :mock
               :teams {"1" {:position [0 0]}
                       "2" {:position [0 1]}}}
              {:team-name "1" :command-name "move" :direction :right}))
           {:layout [[::c/empty ::c/monkey ::c/monkey]]
            :original-layout [[::c/empty ::c/empty ::c/empty]]
            :random :mock
            :teams {"1" {:position [0 1]}
                    "2" {:position [0 2] :buffs {::c/tackled 2}}}})))

(deftest test-pick-up-item
  (is (= {:layout [[::c/monkey ::c/empty]]
          :teams {"1" {:position [0 0]
                       :picked-up-items [::c/song]}}} 
         (c/run-command
           {:layout [[::c/monkey ::c/song]]
            :teams {"1" {:position [0 0]
                         :picked-up-items []}}}
           {:team-name "1" :command-name "move" :direction :right}))))

(deftest test-apply-sleep-buff
  (is (nil? (c/apply-buffs
              {:teams {"1" {:buffs {::c/asleep 1}}}}
              {:command-name "move" :team-name "1" :direction "right"}))))

(deftest test-apply-no-buff
  (is (= (c/apply-buffs
           {:teams {"1" {:buffs {}}}}
           {:command-name "move" :team-name "1" :direction "right"})
         {:command-name "move" :team-name "1" :direction "right"})))

(deftest test-apply-speedy-buff
  (is (= (c/apply-buffs
           {:teams {"1" {:buffs {::c/speedy 1}}}}
           {:command-name "move" :team-name "1" :directions ["right", "left"]})
         [{:command-name "move" :team-name "1" :direction "right"}
          {:command-name "move" :team-name "1" :direction "left"}])))

(deftest test-apply-all-buffs
  (is (= (c/apply-all-buffs
           {:teams {"1" {:buffs {::c/speedy 1}}
                    "2" {:buffs {::c/asleep 1}}}}
           [{:command-name "move" :team-name "1" :directions ["right", "left"]}
            {:command-name "move" :team-name "2" :direction "right"}])
         [{:command-name "move" :team-name "1" :direction "right"}
          {:command-name "move" :team-name "1" :direction "left"}])))

(deftest test-team-at
  (is (= (c/team-at
           {:teams {"1" {:position [0 1]}
                    "2" {:position [0 2]}}}
           [0 2])
         "2")))

(deftest test-add-buff
  (is (= (c/add-buff {:teams {"1" {:buffs {}}}} "1" ::c/speedy)
         {:teams {"1" {:buffs {::c/speedy (c/buff-duration ::c/speedy)}}}})))

(test-ns 'monkey-music.core-test)
