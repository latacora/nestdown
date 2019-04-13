(ns nestdown.cli
  (:require
   [clojure.tools.cli :as cli]
   [clojure.string :as str]
   [cheshire.core :as json]
   [clojure.edn :as edn])
  (:gen-class))

(def ^:private parsers
  {:json json/parse-stream
   :edn edn/read})

(def ^:private cli-opts
  [["-h" "--help" "Display help message"]
   ["-f" "--format FORMAT" "Input format"
    :id :parser
    :default :json
    :parse-fn #(-> %
                   str/lower-case
                   keyword
                   parsers)
    :validate-fn [some?
                  (->> parsers keys (str/join ", ") (str "format must be one of "))]]])

(defn ^:private usage
  [opts-summary]
  (->> ["Converts nested data structures into markdown."
        ""
        "Usage: nestdown [OPTIONS] < infile > outfile"
        ""
        "Input is only read from stdin, output only written to stdout."
        ""
        "Options:"
        opts-summary]
       (str/join \newline)))

(defn ^:private error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn ^:private validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-opts)]
    (cond
      (or (:help options) (= :help (first arguments)))
      {:exit-message (usage summary) :ok true}

      errors
      {:exit-message (error-msg errors) :ok false}

      :else
      {:opts options})))

(defn ^:private exit!
  [message code]
  (println message)
  (System/exit code))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [opts exit-message ok]} (validate-args args)
        {:keys [parser]} opts]
    (when exit-message (exit! exit-message (if ok 0 1)))
    (-> *in* parser  )))
