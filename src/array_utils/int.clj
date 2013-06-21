(ns ^{:doc "Utilities for int[]"
      :author "EHF"}
  array-utils.int
  (:use array-utils.core)
  (:refer-clojure :exclude [amap]))

(set! *warn-on-reflection* true)

;; # Int implementations

;; Please refer to `core.clj/abind-hint` for information on how the
;; bindings work.

(def type-info
  {:sg `int
   :atype "[I"
   :constructor `int-array
   :min-value Integer/MIN_VALUE
   :max-value Integer/MAX_VALUE})

(load "type_impl")
