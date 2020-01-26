
Lilac: some validation functions in ClojureScript
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/mvc-works/lilac.svg)](https://clojars.org/mvc-works/lilac)

```edn
[mvc-works/lilac "0.0.2"]
```

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

If data does not pass validation, you may find by `:ok? false` and got message:

```clojure
(validate-lilac data lilac-demo+) ; {:ok? false, :formatted-message "..."}
```

### Contribute to project

If you like the idea in Lilac, fork the project and develop on your own intention. This project does not accept large changes.

The project is developed based on Cirru toolchains. Clojure code are compiled from `calcit.cirru`. Make sure you are using [Calcit Editor](https://github.com/Cirru/calcit-editor) if you need the fix to be merged.

### Naming

Since Lilac has APIs similar to `number` `or` `and` `vector`, which are core functions/variables in Clojure. I have to add prefix/suffix in names.

Lilac uses suffix of `+` in APIs, why? Look at this picture:

![lilac picture](assets/lilac-720x480.jpg)

### License

MIT
