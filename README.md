# graph-fnk-viz

Vizualize prismatic graph with rhizome

## Usage

```clojure
(require '[plumbing.core :refer [fnk]])
(require '[graph-fnk-viz :as viz])

(def graph
  {:a (fnk [b c] (* b c))
   :b (fnk [input-d] (inc input-d))
   :c (fnk [input-e] (dec input-e))
   :f (fnk [a [:subgraph g]] (* a g))
   :subgraph {:g (fnk [h i j] (+ h i j))
              :h (fnk [i j] (+ i j))
              :i (fnk [input-d] (* input-d input-d))
              :j (fnk [input-e] (dec input-e))}})

(viz/view-graph graph)
(viz/save-svg graph "graph.svg")
(viz/save-png graph "graph.png")
```

## License

Copyright © 2014 Red Brain Labs

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
