(ns graph-vizualizer
  (:require [plumbing.fnk.pfnk :refer [io-schemata]]
            [plumbing.graph :as graph]
            [rhizome.viz :as viz]
            [schema.core :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Private Helpers

(defn schema-deps
  ([schema] (schema-deps [] schema))
  ([parent-path schema]
    (->> schema
         (filter (fn [[k v]] (s/specific-key? k)))
         (mapcat (fn [[k v]]
                   (let [path (conj parent-path (s/explicit-schema-key k))]
                     (if (= clojure.lang.PersistentArrayMap (type v))
                       (schema-deps path v)
                       [path]))))
         (into #{}))))

(defn conditional-into [pred coll val]
  (if (pred val)
    (do (into coll val))
    val))

(defn graph-deps
  ([graph] (graph-deps [] graph))
  ([path graph]
    (let [deps (-> graph graph/eager-compile io-schemata first schema-deps)
          init (zipmap deps (repeat #{}))]
      (reduce (fn [d [k n]]
                (if (map? n)
                  (merge d (graph-deps (conj path k) n))
                  (let [cleaned-deps (->> (io-schemata n)
                                          first
                                          schema-deps
                                          (map (partial conditional-into (complement deps) path))
                                          (into #{}))]
                    (assoc d (conj path k) cleaned-deps))))
              init graph))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Public

(defn save-graph [graph writeable]
  (let [al (graph-deps graph)
        svg (viz/graph->svg (keys al) al
             :node->descriptor (fn [node] {:label (peek node)})
             :node->cluster (fn [node] (if (> (count node) 1) (pop node) nil))
             :cluster->descriptor (fn [cluster] {:label (peek cluster)})
             :cluster->parent (fn [cluster] (if (> (count cluster) 1) (pop cluster) nil))
             :options {:dpi 50})]
    (spit writeable svg)))
