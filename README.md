
Lilac: some validation functions in ClojureScript
----

### Usage

[![Clojars Project](https://img.shields.io/clojars/v/mvc-works/lilac.svg)](https://clojars.org/mvc-works/lilac)

```edn
[mvc-works/lilac "0.1.4-a2"]
```

```clojure
(require '[lilac.core :refer [validate-lilac number+ string+ or+]])

(validate-lilac 1 (number+))

(validate-lilac 1
  (or+ [(number+) (string+)]))
```

If validation is passed, it returns:

```edn
{:ok? true}
```

or the return value would contain informations why it's not correct:

```edn
{:ok? false
 :data x
 :rule x
 :coord []
 :message "failure reason..."
 :formatted-message "formatted failure reason..."}
```

#### APIs

Supported APIs:

```clojure
(:require [lilac.core :refer [validate-lilac deflilac register-custom-rule!
           optional+ keyword+ boolean+ number+ string+ custom+ tuple+ vector+
           list+ record+ enum+ not+ and+ map+ any+ set+ nil+ or+ is+ dev-check]])
```

For example:

```clojure
(validate-lilac 10 (and+ [(number+) (number+ {:min 0})]))

(validate-lilac 10 (or+ [(number+) (string+)]))

(validate-lilac nil (optional+ (number+)))

(validate-lilac :a (enum+ #{:a :b :c}))

(validate-lilac
  {:a 100, :b ["red" "blue"]}
  (record+ {:a (number+)} {:exact-keys? true}))

(deflilac lilac-good-number+ (n) (number+ {:min n}))
```

Or use a shortcut with `js/console.error`, which only runs where `js/goog.DEBUG` is true:

```clojure
(dev-check 1 (number+))
```

Notice:

* in Lilac, a "map" with specific keys are called a "record". Use `map+` for dictionaries.
* `tuple+` is a vector with each item in specific type. Use `:in-list?` for list instead of vector.

Meanings of record options:

* `:exact-keys?`, keys are exactly the same as rules, no more no fewer
* `:check-keys?`, keys are inside the rules, no more
* `:all-optional?`, mark all keys as optional

For more details browse source code:

* https://github.com/mvc-works/lilac/blob/master/src/lilac/test.cljs
* https://github.com/mvc-works/lilac/blob/master/src/lilac/router.cljs

For vectors and lists, `:allow-seq? true` to accept lazy sequences.

#### Recursive data

Lilac is designed to validate recursive data, based on a "component" concept behind `deflilac`:

```clojure
(require '[lilac.core :refer [deflilac record+ string+]])

(deflilac lilac-tree+ []
  (record+ {:name (string+)
         :children (vector+ (lilac-tree+))}))
```

#### Custom rules

To provide `lilac.core/custom+`:

```clojure
(defn method-1 [x]
  (if (and (> x 10) (< x 20))
    {:ok? true}
    {:ok? false, :message (str "expects number between 10 amd 20, got " x)}))

(validate-lilac 11 (custom+ method-1))
```

To added custom validation type called `method-2+` (something like `number+`), use an API:

```clojure
(defn validate-method-2 [data rule coord]
  (if (and (> data 10) (< data 20))
    {:ok? true}
    {:ok? false,
     :data data,
     :rule rule,
     :coord coord,
     :message (str "expects number between 10 amd 20, got " data)}))

(defn method-2+ [] {:lilac-type :method-2})

(lilac.core/register-custom-rule! :method-2 validate-method-2)

(validate-lilac 11 (method-2+))
```

### Contribute to project

If you like the idea in Lilac, fork the project and develop on your own intention. This project does not accept large changes.

The project is developed with Cirru toolchains. Clojure code are compiled from `calcit.cirru`. Make sure you are using [Calcit Editor](https://github.com/Cirru/calcit-editor) if you need the fix to be merged.

### Naming

Since Lilac has APIs similar to `number` `or` `and` `vector`, which are core functions/variables in Clojure. I have to add prefix/suffix in names. Lilac uses suffix of `+` in APIs, inspired by [lilac](assets/lilac-720x480.jpg).

### License

MIT
