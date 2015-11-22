# boot-gettext

[](dependency)
```clojure
[delaguardo/boot-gettext "0.1.2"] ;; latest release
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

Then slice translations from your source files

```clj
(gettext :locale "jp")
```

or from command line:

```bash
boot gettext -l "jp"
```

This will generate directory `./target/locales`. Just move it to project's resource folder like `cp -r ./target/locales ./resources`.

## Edit translation dictionary

Freshly generated translation file (`./resources/locales/jp.tr.edn`) looks like:

```clojure
{"My name is " {:value "My name is "
                :disabled? true}}
```

A key is a origin string spliced from source code, :value define transaltion and :disabled show will it use :value for rewriting source code. Change it to this:

```clojure
{"My name is " {:vlaue "私の名前は"
                :disabled? false}}
```

## License

Copyright © 2015 Kirill Chernyshov

Distributed under the Eclipse Public License either version 1.0 or (at
                                                                    your option) any later version.

[1]: https://github.com/boot-clj/boot
