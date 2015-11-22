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

(defn- rename-match [match]
  (zipmap [:origin :transform :str-key] match))

(defn- prepare-translations! [text translations]
  (let [keys (map :str-key (dedupe (map rename-match (re-seq #"\#(t|i|p) \"(.*)\"" text))))]
    (reset! translations (merge (zipmap keys keys)
                                @translations))))

(defn- interpolate [st]
  )

(defn- pluralize [st]
  )

(defn- replace-part [text k v]
  (let [pattern (re-pattern (str "#(t|i|p) \"\\Q" k "\\E\""))]
    (loop [t text stop false]
      (if stop
        t
        (let [[_ l] (re-find pattern t)]
          (if-not l
            (recur t true)
            (let [p (re-pattern (str "#" l " \"\\Q" k "\\E\""))]
              (recur (clojure.string/replace
                      text
                      p
                      (case l
                        "t" (str "\"" v "\"")
                        "i" (str "\"" v "\"") ;; TODO Implement intepolation
                        "p" (str "\"" v "\"") ;; TODO Implement pluralization
                        (str "\"" v "\"")))
                     false))))))))

(defn- translate-file! [text out-file translations]
  (loop [t text tr @translations]
    (let [[k v] (first tr)]
      (if-not k
        t
        (recur
         (replace-part t k v)
         (rest tr))))))

(core/deftask gettext
  "Translate .clj/.cljs/.cljc source files with translations from {locale}.tr.edn source file."
  [l locale LOCALE str "A locale identificator."]
  (let [out-dir (core/tmp-dir!)
        last-source-files (atom nil)
        translations (atom nil)]
    (core/with-pre-wrap fileset
      (let [source-files (->> fileset
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
                  (spit (translate-file! in-file-text out-file translations))))))
          (util/info "Write %s\n" out-translations-path)
          (write-translations-edn! out-translations-file translations))
        (-> fileset
            (core/add-resource out-dir)
            core/commit!)))))
