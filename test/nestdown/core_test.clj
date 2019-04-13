(ns nestdown.core-test
  (:require [clojure.string :as str]
            [clojure.test :as t]
            [nestdown.core :as n]))

(t/deftest nested->md-tests
  (t/is (=
         (->> ["* key: value"]
              (str/join "\n"))
         (n/nested->md {"key" "value"})))
  (t/is (=
         (->> ["* key one: value"
               "* key two: value"]
              (str/join "\n"))
         (n/nested->md {"key one" "value"
                           "key two" "value"})))
  (t/is (=
         (->> ["* key"
               "    * val1"
               "    * val2"
               "    * val3"]
              (str/join "\n"))
         (n/nested->md {"key" ["val1" "val2" "val3"]})))
  (t/is (=
         (->> ["* key"
               "    * nested"
               "        * deeply: value"]
              (str/join "\n"))
         (n/nested->md {"key" {"nested" {"deeply" "value"}}})))

  (t/testing "inline"
    (t/is (= "a" (n/format-inline "a")))
    (t/is (=
           (->> ["* key"
                 "    * nested: an, inline, sequence"]
                (str/join "\n"))
           (n/nested->md {"key" {"nested" ^::n/inline ["an" "inline" "sequence"]}})))
    (t/is (=
           (->> ["* key"
                 "    * nested (an: inline, map: of values)"]
                (str/join "\n"))
           (n/nested->md {"key" {"nested" ^::n/inline {"an" "inline" "map" "of values"}}})))
    (t/is (=
           (->> ["* key"
                 "    * nested: an, inline, sequence, followed by, an: inline, map: of values"]
                (str/join "\n"))
           (n/nested->md {"key" {"nested" ^::n/inline ["an" "inline" "sequence" "followed by" {"an" "inline" "map" "of values"}]}}))))

  (t/testing "nested structures as map keys"
    (t/is
     (=
      "* nested: key: simple value" ;; This looks a little weird but I can't think of a better way to format it...
      (n/nested->md {{"nested" "key"} "simple value"})))
    (t/is
     (=
      "* nested: deeply: key-value: simple value"
      (n/nested->md {{"nested" {"deeply" "key-value"}} "simple value"})))
    (t/is
     (=
      (->> ["* nested: deeply: key-value"
            "    * nested"
            "        * deeply: value"]
           (str/join "\n"))
      (n/nested->md {{"nested" {"deeply" "key-value"}} {"nested" {"deeply" "value"}}})))
    (t/is
     (=
      (->> ["* nested: deeply: key-value"
            "    * nested"
            "        * deeply: value"]
           (str/join "\n"))
      (n/nested->md {{"nested" {"deeply" "key-value"}} {"nested" {"deeply" "value"}}})))
    (t/is
     (=
      (->> ["* sequence (deeply: key-value)"
            "    * nested"
            "        * deeply: value"]
           (str/join "\n"))
      (n/nested->md {["sequence" {"deeply" "key-value"}] {"nested" {"deeply" "value"}}})))
    (t/is
     (=
      (->> ["* map: one (map: two)"
            "    * some"
            "        * nested: details"]
           (str/join "\n"))
      (n/nested->md {[{"map" "one"} {"map" "two"}] {"some" {"nested" "details"}}})))
    (t/is
     (=
      (->> ["* a: b, c: d"
            "    * e: f"
            "    * g: h"]
           (str/join "\n"))
      (n/nested->md {{"a" ["b" {"c" "d"}]} {"e" "f" "g" "h"}})))
    (t/is
     (=
      (->> ["* a: b: c" ;; not sure if this is what i want but at least we know when it regresses
            "    * x: y"
            "    * p: q"]
           (str/join "\n"))
      (n/nested->md {{"a" [{"b" "c"}]} {"x" "y" "p" "q"}})))))
