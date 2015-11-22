# boot-gettext

[](dependency)
```clojure
[delaguardo/boot-gettext "0.1.1"] ;; latest release
```
[](/dependency)

Task for the [boot Clojure build tool][1].

* Provides `gettext` tasks
* Parses a `*.clj`, `*.cljs` and `*.cljc` files to configure keys in translations dictionary
* Parses a `{locale}.tr.edn` files to fill translations. If translation not provided for specific key it use key by itself like translations.

## Usage

Add `boot-gettext` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[delaguardo/boot-gettext "0.1.0-SNAPSHOT" :scope "test"]])
(require '[delaguardo.boot-gettext :refer [gettext]])
```

Then start translate your source files:

```clj
(deftask build
  (comp
  (gettext :locale "ru")
  ;; All other build tasks, (cljs) or (jar) for example.
))
```

## License

Copyright © 2015 Kirill Chernyshov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: https://github.com/boot-clj/boot
