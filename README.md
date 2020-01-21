
Lilac: some validation functions in ClojureScript
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/mvc-works/lilac.svg)](https://clojars.org/mvc-works/lilac)

```clojure
(require '[lilac.core :refer [validate-lilac lilac-number lilac-string lilac-or]])

(validate-lilac 1 (lilac-number nil))

(validate-lilac 1
  (lilac-or (lilac-number nil) (lilac-string nil)))
```

### License

MIT
