(ns devtools.util)

(defmacro ocall [o name & params]
  `(let [o# ~o]
     (.call (goog.object/get o# ~name) o# ~@params)))

(defmacro oapply [o name param-coll]
  `(let [o# ~o]
     (.apply (goog.object/get o# ~name) o# (into-array ~param-coll))))

(defmacro oget
  ([o k1] `(goog.object/get ~o ~k1))
  ([o k1 k2] `(when-let [o# (goog.object/get ~o ~k1)]
                (goog.object/get o# ~k2)))
  ([o k1 k2 & ks] `(when-let [o# (goog.object/get ~o ~k1)]
                     (oget o# ~k2 ~@ks))))

(defmacro oset [o ks val]
  (let [keys (butlast ks)
        obj-sym (gensym)]
    `(let [~obj-sym ~o
           target# ~(if (seq keys) `(devtools.util/oget ~obj-sym ~@keys) obj-sym)]
       (assert target# (str "unable to locate object path " ~keys " in " ~obj-sym))
       (goog.object/set target# (last ~ks) ~val)
       ~obj-sym)))

(defmacro safe-call [f exceptional-result & args]
  `(try
     (~f ~@args)
     (catch :default e#
       ~exceptional-result)))

(defn compiler-in-dev-mode? []
  (if cljs.env/*compiler*
    (let [mode (get-in @cljs.env/*compiler* [:options :optimizations])]
      (or (= mode :none) (nil? mode)))))                                                                                      ; I'm not sure if mode must be always specified, defaults to :none if not

(defmacro emit-if-compiler-in-dev-mode [body]
  (if (compiler-in-dev-mode?)
    body))
