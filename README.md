hiphip (array)!
===========

`hiphip` is an array library for Clojure, whicih provides elegant
methods for fast math with primitive arrays.

Leiningen dependency (Clojars): [prismatic/hiphip "0.1.0"]

**This is an alpha release.  The API and organizational structure are subject to change.  Comments and contributions are much appreciated.**

# Why?

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

# About

`hiphip` provides functions and macros that require little or no
manual type hinting, and they use a binding semantics similar to those
of `for` (see `Bindings` below). They are explained below.

The library currently supports arrays of floats, doubles, ints, and
longs. You can extend to other types by providing type information to
the functions in `hiphip.array`. Please see `DEVELOPERS.md` for more
information.

Note: if you don't need the speed of primitive arrays, we encourage you
to keep using Clojure's 'map' and 'reduce' -- they're more flexible.

# Usage

In your `project.clj`, add a dependency on 
`[hiphip "unreleased-version"]`. Then require the namespace for your type, e.g.

```clojure 
(require 'hiphip.double) 
```

# Bindings

Bindings look like this:

```clojure
(au/amap
  [[i x] xs]
  <expression involving i, x>)
```

This binds `i` to the current index and `x` to the ith element of xs.
The index-variable is optional, but there must be at least one array
binding. You can have as many array bindings as you want. For example:

```clojure
(au/amap
  [x xs
   y ys 
   z zs...]
  <expression involving x, y>)
```

Iteration is parallel and not nested, ulike `for` and `doseq.
Therefore, in

```clojure
(au/amap
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
(au/afill!
  [[i x] xs
   :range [0 10]]
  i)
```

### Let

The bindings also support let, which works like a regular `let` in the
inner loop, but casts to the array type (for speedy math), e.g.

```clojure
(au/afill!
  [x xs
  :let [alpha 5 delta (- x 9)]]
  (* x alpha delta)) 
```

Be aware that `:let` explicitly disallows shadowing the array
bindings, e. g. `(afill! [myvar xs :let [myvar 5]] myvar)` throws an
`IllegalArgumentException`. Do also note that destructuring syntax is
not supported.

# API overview

## Iteration

These macros use the bindings explained above. For more examples,
please see the docstrings.

### areduce

```clojure
;; The maximum ratio entries of two vectors.
(au/areduce [x xs y ys] result 1 (max result (/ x y)))
```

### asum

```clojure
;; dot product
(au/asum [x xs y ys] (* x y))
;; Compute a power series.
(let [x 2.0]
  (au/asum [[i a] as] (* a (Math/pow x i))))
;; Sum the first 10 elements of the array.
(au/asum [x xs :range [0 10]] x)
```

### aproduct

```clojure
;; Compute a joint probability.
(let [scale 3.0]
  (au/aproduct [x xs] (/ x scale)))
```

### amap

Builds a new array from evaluating the expression at each step.

```clojure
;; Take the max of two arrays.
(au/amap [x xs y ys] (max x y))
```

### afill!

Like `amap`, but instead of building a new array, changes the *first*
array in-place.

```clojure
;; Add a constant to an array.
(au/afill! [x xs] (+ x 1.0))
;; The += operation for two arrays.
(au/afill! [x xs y ys] (+ x y))
;; Insert marker values for each negative x.
(au/afill! [x xs] (if (< 0 x) 999 x))
```

### doarr

Like `doseq`, but for arrays. Presumably used for side-effects.

```clojure
;; Apply some java object's function.
(let [java-thing (JavaThing.)]
  (au/doarr [[i x] xs y ys] (.process java-thing i x y))
  (.getResult java-thing))
```

# Array functions

## Math

```clojure
;; Sum of an array
(asum xs)
;; Product (big pi) of an array
(aproduct xs)
;; Mean over an array
(amean xs)
;; Dot product of two arrays
(dot-product xs ys)
```

## Utility

We provide pre-hinted versions of `aclone` et. al, in addition to a
couple of other useful functions.

```clojure
;; Length of an array.
(au/alength xs)
;; Clones an array.
(au/aclone xs)
;; Get the 2nd element.
(au/aget xs 2)
;; Set the 2nd element to 42.
(au/aset xs 2 42)
;; Increment the 2nd element by 57.
(au/ainc 2 57)
;; Make a new array with 10 000 elements generated with `rand`. Bind
;; the index-variable to idx.
(au/amake [idx 42] (rand idx)) 
```

## Sorting and minimum/maximum

Note: These are mostly written in Java for pure speed, as it was
difficult to match Java's performance for in-place sorting.

```clojure
;; Maximum over an array
(au/amax xs)
;; Minimum over an array
(au/amin xs)
;; Index of maximum
(au/amax-index xs)
;; Index of minimum
(au/amin-index)

;; Sort array in-place
(au/asort! max)
;; Sort xs using a pivot.
(au/apartition! xs 5.4)
;; Modifies xs s. t. the smallest 42 elements come first, followed by
;; all greater elements. 
(au/select! xs 42)
;; Sort array in-place, so that the last 42 elements are the largest
;; 42 elements in descending order 
(au/asort-max! xs 42)
;; Sort array in-place, so that the first 42 elements are the smallest
;; 42 elements in descending order 
(asort-min! xs 42)
```

In addition, these functions come in versions either returning or
modifying arrays of indices. Please refer to their docstrings for how
to use them.

# Caveats

New versions of Leiningen set JVM options that might prevent the JVM
from doing some optimizations to your code. If your code seems
unreasonbly slow, make sure to add `:jvm-opts ^:replace []` to your
`project.clj`.

# Supported Clojure versions

hiphip is currently supported on Clojure 1.5.x.

# Contributors

hiphip is the result of a collaboration between Prismatic, Emil Flakk, and Leon Barrett at Climate Corp.

# License

Copyright (C) 2013 Emil Flakk, Leon Barrett, and Prismatic.  Distributed under the Eclipse Public License, the same as Clojure.