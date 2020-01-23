
Lilac: some validation functions in ClojureScript
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/mvc-works/lilac.svg)](https://clojars.org/mvc-works/lilac)

```clojure
(require '[lilac.core :refer [validate-lilac number+ string+ or+]])

(validate-lilac 1 (number+))

(validate-lilac 1
  (or+ (number+) (string+)))
```

Lilac was initially designed to validate recursive data, with a "component" concept begined `deflilac`:

```clojure
(require '[lilac.core :refer [deflilac map+ string+]])

(deflilac lilac-tree+ []
  (map+ {:name (string+)
         :children (vector+ (lilac-tree+))}))
```

To added custom behaviors, do dirty work to:

```clojure
(swap! lilac.core/*custom-methods assoc :x (fn [x...] (x...)))
```

### License

MIT
