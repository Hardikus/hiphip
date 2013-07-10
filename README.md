hiphip (array)!
===========

`hiphip` is an array library for Clojure, whicih provides elegant
methods for fast math with primitive arrays.

Leiningen dependency (Clojars): [prismatic/hiphip "0.1.0-SNAPSHOT"]

**This is an alpha release. The API and organizational structure are
subject to change. Comments and contributions are much appreciated.**

## Why?

Instead of writing

```clojure
;; 1ms for 10000 doubles
(defn dot-product [^doubles ws ^doubles xs]
  (reduce + (map * ws xs))
```

or the faster but messier

```clojure
;; 8.5 us
(defn dot-product [^doubles ws ^doubles xs]
  (areduce xs i ret 0.0
    (+ ret (* (aget xs i)
              (aget ws i))))
```

you can write the fast and simple

```clojure
;; 8.5 us
(defn dot-product [ws xs] (hiphip.double/asum [x xs w ws] (* x w)))
```

## About

`hiphip` provides functions and macros that require little or no
manual type hinting, and they use a binding semantics similar to those
of `for`. The bindings are explained below, and in `hiphip.array`.

The library currently supports arrays of floats, doubles, ints, and
longs. You can extend to other types by providing type information to
the functions in `hiphip.array`. Please see `DEVELOPERS.md` for more
information.

## Tour de force

Usually numerical code in Clojure is either flexible or fast. Why not
both? Suppose you want do calculate the joint probability of an array
of probabilities. You write this one-liner:

```clojure
(dbl/aproduct xs)
```

It would be nice to normalize the probabilities, such that their sum
equals one. However, the array is somewhat large, so you decide to do
it in-place. You add this function:

```clojure
(defn normalize! [xs]
  (let [sum (dbl/asum xs)]
    (dbl/afill! [x xs] (/ x sum))))
```

But times are a-changing. You now wish to weight the probabilities in
xs (with weights contained in ys) as well. All this without creating a
new array, but instead overwriting the old xs array. No problem.

```clojure
(defn weight-and-normalize! [xs ys]
  (do (dbl/afill! [x xs y ys] (* x y))
      (normalize! xs)))
```

Next, your boss tells you to write a function for finding the
frequencies of the different probabilities in xs. It's used in some
important services, and therefore needs to be fast. Simply use areduce
and transients.

```clojure
(defn afrequencies [xs]
  (persistent!
    (dbl/areduce [x xs] ret (transient {})
      (assoc! ret x (inc (get ret x 0))))))
```

At the end of the day, there's still a bit left to do. You realise
it'd be darn nice have a function that calculates the standard
deviation.

```clojure
(defn std-dev [xs]
  (let [mean (dbl/amean xs)
        square-diff-sum (dbl/asum [x xs] (Math/pow (- x mean) 2))]
    (/ square-diff-sum (dbl/alength xs))))
```

A simple statistics engine. Done.

## API overview

HipHip provides typed namespaces (e.g. `hiphip.double` or
`hiphip.long`) for each supported array type. These namespaces
provide:

* A family of macros for efficiently iterating over array(s) with a
  common binding syntax, including `doarr`, `afill!` (in-place), and
  our own versions of `amap` and `areduce`.

* Pre-hinted versions of Clojure built-ins like `alength` and `aset`,
  in addition to utility functions like `ainc` and `amake`.

* Common math utils like `asum` and `aproduct` (both with optional
  bindings), as well as `amean` and `dot-product`.

* Sorting (in-place) and max/min functions (written in Java for pure
  speed), with additional varities that work on or return arrays of
  indices.

For general looping needs, we provide `hiphip.array`. The API is more
limited, but allows you to efficiently loop through arrays of
different types, provided they are type-hinted properly.

## Bindings

The looping macros use bindings to efficiently iterate through one or
several arrays. They look like this:

```clojure
(dbl/amap
  [[i x] xs]
  <expression involving i, x>)
```

This binds i to the current index and x to the ith element of xs. The
index-variable is optional, but there must be at least one array
binding. You can have as many array bindings as you want. For example:

```clojure
(dbl/amap
  [x xs
   y ys 
   z zs...]
  <expression involving x, y>)
```

Iteration is parallel and not nested, ulike `for` and `doseq.
Therefore, in

```clojure
(dbl/amap
  [[i1 x] xs
   [i2 y] ys
   [i3 z] zs ...]
  <expression involving i1, x, i2, y, i3, z>)
```

the index-variables i1, i2, and i3 will have the same value.

### Ranges

You can specify a range for the operations. The default range is from
0 to the length of the first array.

```clojure
(dbl/afill!
  [[i x] xs
   :range [0 10]]
  i)
```

### Let

The bindings also support let, which works like a regular `let` in the
inner loop, but casts to the array type (for speedy math), e.g.

```clojure
(dbl/afill!
  [x xs
  :let [alpha 5 delta (- x 9)]]
  (* x alpha delta)) 
```

Be aware that `:let` explicitly disallows shadowing the array
bindings, e.g. `(afill! [myvar xs :let [myvar 5]] myvar)` throws an
`IllegalArgumentException`. Do also note that destructuring syntax is
not supported.

## Running the tests

You can run various portions of the test suite using leiningen test selectors.  
 
* `lein test :fast` runs just the correctness tests, and should be
   fast.
 
* `lein test :gen-test` runs generative tests, which are also fast but
   print a lot.

* `lein test :bench` runs the benchmarks, which are very slow, and
  also a bit flaky currently (due to jitter in timing runs). You can
  look in `test/hiphip/array_test.clj` for the generic array
  tests/benchmarks, and `test/hiphip/type_impl_test.clj` for the
  type-specific benchmarks, which include current expected performance
  numbers vs. Java for a number of common operations (plus ~10% to
  minimize flakiness). Some things are much slower than we would like
  currently, but most operations on double arrays are within 0-50% of
  Java speed.

## Known issues

There are still a few performance issues we're working on. For
instance, math on index variables and doubles is several times slower
than Java:

```clojure
(hiphip/afill! [[i x] xs] (* x i))
```

Some operations on non-double primitive arrays are also slower (up to
about three times as slow as Java) -- check out the benchmark suite in
`test/hiphip/type_impl_test.clj` for the most up-to-date results. We
welcome contributions of performance improvements, or just benchmarks
where HipHip is slow, so we can all work together towards making it
easy to write maximally performant math in Clojure.

## Performance: know your options

Achieving maximal performance for some HipHip operations required a
lot of fiddling, and some of the most important things we found
involved options to Clojure and the the JVM.

* [Leiningen](http://leiningen.org/) is a great build tool. To help
  speed up slow start-up times (a major concern of users), its
  developers chose to inject options that [disable some advanced
  optimizations](https://github.com/technomancy/leiningen/wiki/Faster#tiered-compilation)
  into your project's JVM (thanks to Stuart Sierra and others on the
  Clojure mailing list for pointing this out). If you want your array
  code to go fast under Leiningen, you probably want to add the
  following to your `project.clj`:

<script src="https://gist.github.com/w01fe/5964036.js"></script>

* Clojure provides an `*unchecked-math*` compiler option to speed up
  primitive math by omitting overflow checks. We've found mixed
  results with this option -- it almost always helps, but in some
  cases (especially with double array operations) it actually hurts
  performance.

## Supported Clojure versions

hiphip is currently supported on Clojure 1.5.x.

## Contributors

hiphip is the result of a collaboration between Prismatic, Emil Flakk, and Leon Barrett at Climate Corp.

## License

Copyright (C) 2013 Emil Flakk, Leon Barrett, and Prismatic.  Distributed under the Eclipse Public License, the same as Clojure.