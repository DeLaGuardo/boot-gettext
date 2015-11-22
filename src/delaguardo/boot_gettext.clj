(ns delaguardo.boot-gettext
  {:boot/export-tasks true}
  (:require [clojure.java.io :as io]
            [boot.core       :as core]
            [boot.util       :as util])
  (:import [java.io PushbackReader]))

(defn- write-translations-edn! [out-file translations]
  (io/make-parents out-file)
  (with-open [w (io/writer out-file)]
    (binding [*out* w]
      (clojure.pprint/write @translations))))

(defn- read-file [file]
  (if file
    (with-open [r (PushbackReader. (io/reader (core/tmp-file file)))]
      (binding [*read-eval* false]
        (read r)))
    {}))

(defn- prepare-translations! [text translations]
  (let [from-text (dedupe (map last (re-seq #"\"(.*?)\"" text)))]
    (doseq [tr-entry from-text]
      (when-not (= tr-entry "")
        (swap! translations assoc-in [tr-entry :value] (or (-> @translations (get tr-entry) :value)
                                                           tr-entry))
        (when (nil? (-> @translations (get tr-entry) :disabled?))
          (swap! translations assoc-in [tr-entry :disabled?] true))))))

(defn- replace-part! [text k v translations]
  (if (:disabled? v)
    text
    (let [pattern (re-pattern (str "\"\\Q" k "\\E\""))]
      (if (re-find pattern text)
        (clojure.string/replace text pattern (str "\"" (:value v) "\""))
        text))))

(defn- translate-file! [text translations]
  (loop [t text tr @translations]
    (let [[k v] (first tr)]
      (if-not k
        t
        (recur
         (replace-part! t k v translations)
         (rest tr))))))

(core/deftask gettext
  "Translate .clj/.cljs/.cljc source files with translations from {locale}.tr.edn source file."
  [l locale LOCALE str "A locale identificator."]
  (let [out-dir (core/tmp-dir!)
        last-source-files (atom nil)]
    (core/with-pre-wrap fileset
      (let [translations (atom nil)
            source-files (->> fileset
                              (core/fileset-diff @last-source-files)
                              core/input-files
                              (core/by-ext [".clj" ".cljs" ".cljc"]))
            in-translations-file (->> fileset
                                      (core/input-files)
                                      (core/by-name [(str locale ".tr.edn")])
                                      first)
            out-translations-path (if in-translations-file
                                    (core/tmp-path in-translations-file)
                                    (str "locales/" locale ".tr.edn"))
            out-translations-file (io/file out-dir out-translations-path)]
        (reset! translations (read-file in-translations-file))
        (when (seq source-files)
          (util/info "Translate source files... %d changed files.\n" (count source-files))
          (doseq [f source-files]
            (let [in-path (core/tmp-path f)
                  in-file (core/tmp-file f)
                  out-file (io/file out-dir in-path)]
              (let [in-file-text (slurp in-file)]
                (prepare-translations! in-file-text translations)
                (doto out-file
                  io/make-parents
                  (spit (translate-file! in-file-text translations))))))
          (util/info "Write %s\n" out-translations-path)
          (write-translations-edn! out-translations-file translations))
        (-> fileset
            (core/add-resource out-dir)
            core/commit!)))))
