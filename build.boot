(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure "1.7.0"       :scope "provided"]
                 [boot/core           "2.2.0"       :scope "provided"]
                 [adzerk/bootlaces    "0.1.12"      :scope "test"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.1.2")

(bootlaces! +version+)

(task-options!
 pom {:project     'delaguardo/boot-gettext
      :version     +version+
      :description "Boot task for translate source files same as gettext"
      :url         "https://github.com/DeLaGuardo/boot-gettext"
      :scm         {:url "https://github.com/DeLaGuardo/boot-gettext"}
      :license     {"EPL" "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask dev
  "Dev process"
  []
  (comp
   (watch)
   (repl :server true)
   (build-jar)))
