(ns graph-vizualizer-test
  (:require [clojure.test :refer :all]
            [graph-vizualizer :refer :all]

            [clojure.java.io :as io]
            [plumbing.core :refer [fnk]]))

(def simple-graph
  {:a (fnk [b c] (* b c))
   :b (fnk [input-d] (inc input-d))
   :c (fnk [input-e] (dec input-e))})

(def nested-graph
  {:a (fnk [b c] (* b c))
   :b (fnk [input-d] (inc input-d))
   :c (fnk [input-e] (dec input-e))
   :f (fnk [a [:subgraph g]] (* a g))
   :subgraph {:g (fnk [h i j] (+ h i j))
              :h (fnk [i j] (+ i j))
              :i (fnk [input-d] (* input-d input-d))
              :j (fnk [input-e] (dec input-e))}})

(deftest test-graph-deps
  (testing "generate deps for simple-graph"
    (is (= (graph-deps simple-graph)
           {[:a] #{[:b] [:c]}
            [:b] #{[:input-d]}
            [:c] #{[:input-e]}
            [:input-d] #{}
            [:input-e] #{}})))

  (testing "generate deps for nested-graph"
    (is (= (graph-deps nested-graph)
           {[:a] #{[:b] [:c]}
            [:b] #{[:input-d]}
            [:c] #{[:input-e]}
            [:f] #{[:subgraph :g] [:a]}
            [:subgraph :g] #{[:subgraph :i] [:subgraph :h] [:subgraph :j]}
            [:subgraph :h] #{[:subgraph :i] [:subgraph :j]}
            [:subgraph :i] #{[:input-d]}
            [:subgraph :j] #{[:input-e]}
            [:input-d] #{}
            [:input-e] #{}}))))
