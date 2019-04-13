(ns nestdown.core
  (:require
   [com.rpl.specter :as sr]
   [clojure.string :as str]))

(def ^:private self-descriptive-pair?
  "Is this key-value pair self-descriptive?

  A key value pair is self-descriptive if it is in a set of specifically known
  self-descriptive keys (e.g. regions in the context of AWS; it's clear what
  us-west-1 means), or if the value starts with the key as a substring (e.g.
  again in the context of AWS, subnet-123abc is clearly a subnet)."
  (let [canonical (comp str/lower-case name)]
    (some-fn
     (comp some? #{"name" "region"} canonical key)
     (fn [[k v]] (boolean (str/starts-with? (canonical v) (canonical k)))))))

(defn ^:private format-inline
  [x]
  (->> x
       (sr/transform
        [NESTED]
        (fn [x]
          (cond
            (map? x) (format-details x)
            (sequential? x) (str/join ", " x)
            :else (str x))))))

(defn ^:private format-details
  "Format some details kv pairs.

  If a pair is self-descriptive, its values are concatenated. Otherwise, keys
  and values are described inline (e.g. color: purple, flavor: grape). A pair
  may be self-descriptive because its value describes the type (e.g.
  vpc-deadbeef is probably a VPC) or because the values are immediately
  recognizable (e.g. us-east-1 does not need describing, it is probably the
  region)."
  [kvs]
  (let [{self-descriptive true not-self-descriptive false} (group-by self-descriptive-pair? kvs)
        vs (map val self-descriptive)
        kvs (map (fn [[k v]] (str k ": " v)) not-self-descriptive)]
    (str/join ", " (concat vs kvs))))

(defn ^:private indented
  [body indent-level]
  (let [indent (apply str (repeat indent-level "    "))]
    (str indent "* " body)))

(defn nested->md
  ([l]
   (->> (nested->md l 0)
        (flatten)
        (str/join "\n")))
  ([l indent]
   (cond
     (map? l)
     (for [[k v] l
           :let [k (if (and (sequential? k) (-> k count (= 2)))
                     (let [[desc details] k]
                       (if (seq details)
                         (format "%s (%s)" (format-inline (first k)) (format-inline (second k)))
                         (format-inline desc)))
                     (format-inline k))]]
       (cond
         ;; Should this thing explicitly be inlined?
         (-> v meta ::inline)
         (format (if (map? v) "%s (%s)" "%s: %s") (indented k indent) (format-inline v))

         (map? v)
         [(indented k indent) (nested->md v (inc indent))]

         (sequential? v)
         (let [[x & xs] v]
           (cond
             ;; Is this thing empty?
             (nil? x)
             (str (indented k indent) ": none")

             ;; Does this thing have one item that fits on a line?
             (and (nil? xs) (not (or (map? x) (sequential? x))))
             (str (indented k indent) ": " x)

             :else ;; no, it's a bigger seq of stuff, walk it
             [(indented k indent) (nested->md v indent)]))

         :else ;; atom
         (let [s (if (some? v)
                   (str k ": " v)
                   k)]
           (indented s indent))))

     (sequential? l)
     (for [elem l] (nested->md elem (inc indent)))

     :else (indented l indent))))
