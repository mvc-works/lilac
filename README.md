
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

Lilac was initially designed to validate recursive data, with a "component" concept begined `deflilac`:

```clojure
(require '[lilac.core :refer [deflilac lilac-map lilac-string]])

(deflilac lilac-tree []
  (lilac-map {:name (lilac-string nil)
              :children (lilac-vector (lilac-tree) nil)} nil))
```

To added custom behaviors, do dirty work to:

```clojure
(swap! lilac.core/*custom-methods assoc :x (fn [x...] (x...)))
```

### License

MIT
