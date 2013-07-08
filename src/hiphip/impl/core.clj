(ns hiphip.impl.core
  "A note on bindings: All these macros use binding forms that look like:

  [[i x] xs
   y ys
   ...]

  This binds i to the index and x and y to the ith element of xs and ys,
  respectively. Note that unlike for/doseq, iteration over multiple arrays
  is parallel rather than nested.

  You can include index variables wherever and whenever you want, so you can do:

  [x xs
   y ys
   ...]

  or:

  [[i1 x] xs
   [i2 y] ys
   [i3 z] zs
   ...]

  but i1, i2, and i3 will have the same value.

  You can also include a range as a first element of the binding:

  [:range [0 10]
   [i x] xs]

  and the operation will only be applied over that range. The default range
  is from 0 to the length of the first array in the binding.

  All of these are internal tools that require type information as
  their first argument. Refer to type_impl.clj, double.clj, long.clj
  et cetera for proper implementations.")

(set! *warn-on-reflection* true)

(defmacro assert-iae
  "Like assert, but throws an IllegalArgumentException not an Error (and also takes args to format)"
  [form & format-args]
  `(when-not ~form (throw (IllegalArgumentException. (format ~@format-args)))))

(defn- find-shadows
  "Finds common variables in two bindings"
  [a b]
  (let [[a-vars b-vars] (map (partial take-nth 2) [a b])]
    (some (set a-vars) b-vars)))

(defn intcast
  "Generate code to cast a symbol to an integer."
  [sym]
  `(clojure.lang.RT/uncheckedIntCast ~sym))

(defn typed-gensym
  "Generate a type-hinted symbol."
  [basis tag]
  (if tag
    (with-meta (gensym basis) {:tag tag})
    (gensym basis)))

(defn parse-binding [index-sym [left right]]
  (case left
    :let {:let-bindings right}
    :range (do (assert-iae (and (vector? right) (= (count right) 2))
                           "Invalid range binding %s; must look like :range [10 20]" right)
               {:range-exprs right})
    ;; else
    (let [[idx-sym val-sym] (if (symbol? left)
                              [nil left]
                              (do (assert-iae (and (vector? left) (every? symbol left))
                                              "Invalid array binding %s: must be either a %s"
                                              left
                                              "val sym or pair of index and value syms")
                                  [(first left) (second left)]))
          array-sym (gensym "arr")]
      {:array-bindings [array-sym right]
       :value-bindings (into (if idx-sym [idx-sym index-sym] [])
                             [val-sym `(aget ~array-sym ~(intcast index-sym))])})))

(defn parse-bindings
  "Given a type, index symbol, and a vector of array bindings, generate a map with keys:
   :start-sym - a symbol bound to the iteration start point
   :stop-sym - a symbol bound to the iteration stop point
   :initial-bindings - bindings [array-sym array-expr ...
                                 start-sym ...
                                 stop-sym ...]
    -- with array-sysm in the order provided in the input.
   :value-bindings - bindings [array-val array-sym ...
                               extra-index-sym index-sym]"
  [bindings]
  (assert-iae (even? (count bindings))
              "Array binding %s requires an even number of forms" bindings)
  (let [index-sym (gensym "i")
        start-sym (typed-gensym "start-sym" long)
        stop-sym (typed-gensym "stop-sym" long)
        {:keys [range-exprs
                array-bindings
                value-bindings
                let-bindings]} (->> bindings
                                    (partition 2)
                                    (map #(parse-binding index-sym %))
                                    (apply merge-with (comp vec concat)))
        [start-expr stop-expr] (cond (empty? range-exprs)
                                     [0 `(alength ~(first array-bindings))]

                                     (= 2 (count range-exprs))
                                     range-exprs

                                     :else
                                     (assert-iae false "Binding has multiple range exprs: %s"
                                                 bindings))]
    (assert-iae (seq array-bindings) "Bindings must include at least one array")
    ;; Do some analysis in case there are conflicting variables
    (let [shadows (find-shadows value-bindings let-bindings)]
      (assert-iae (not shadows)
                  "Variable `%s` shadowed by the let-binding in %s"
                  shadows
                  bindings))
    (assert-iae (and (vector? let-bindings) (even? (count let-bindings)))
                "Invalid let bindings %s; must look like :let [a 1 b 2]" let-bindings)
    {:index-sym index-sym
     :start-sym start-sym
     :stop-sym stop-sym
     :initial-bindings (into array-bindings
                             [start-sym start-expr stop-sym stop-expr])
     :value-bindings (into value-bindings let-bindings)}))

(defn hint-binding [type-info [left right]]
  (case left
    :range [left right]
    :let (->> (partition 2 right)
              (mapcat (fn [[sym val]] `[~sym (~(:etype type-info) ~val)]))
              vec)
    [left (with-meta right {:tag (:atype type-info)})]))

(defn hint-bindings [type-info bindings]
  (assert-iae (even? (count bindings))
              "Array binding %s requires an even number of forms" bindings)
  (->> (partition 2 bindings)
       (mapcat (partial hint-binding type-info))
       vec))

(defmacro dotimes-int
  "Like dotimes, but faster and only works on int ranges.  Also takes an optional
   start for the iteration."
  [[sym & start-stop] & body]
  (let [[start stop] (case (count start-stop)
                       1 (cons 0 start-stop)
                       2 start-stop)]
    `(let [stop# (long ~stop)]
       (loop [~sym (long ~start)]
         (when (< ~sym stop#)
           ~@body
           (recur (unchecked-inc-int ~sym)))))))

(set! *warn-on-reflection* false)