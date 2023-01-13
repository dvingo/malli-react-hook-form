This repo demonstrates how to use [malli](https://github.com/metosin/malli) schemas to validate forms with [react-hook-form](https://react-hook-form.com/get-started)

You can see the running example on github pages:

[dvingo.github.io/malli-react-hook-form](https://dvingo.github.io/malli-react-hook-form/)


To integrate in your own codebase the main point of integration is a malli validation helper
to output errors in the shape used by react-hook-form:

```clojure
(defn use-malli-resolver [schema]
  (let [validator (m/validator schema)
        explain   (m/explainer schema)]
    (hooks/use-callback [schema]
      (fn [data-js]
        (let [data   (js->clj data-js :keywordize-keys true)
              valid? (validator data)]
          (if valid?
            #js{:values data :errors #js{}}
            (let [errs (reduce-kv
                         (fn [errs field messages]
                           (doto errs
                             (g/set (name field) #js{:type "validation" :message (first messages)})))
                         #js{}
                         (me/humanize (explain data)))]
              #js{:values #js{} :errors errs})))))))

;; then in your render function use this "resolver":

(let [resolver      (use-malli-resolver form-schema)
      form-methods  (useForm #js{:resolver resolver})
```

# Running locally

prerequisites:

node+npm
java 
clojure 
yarn 1.x

After cloning the repo:

```bash
yarn install
yarn shadow-cljs watch main
```

navigate to http://localhost:4023

# Credits

The idea of making reusable form input components on top of controlled UI libraries comes from:

https://koprowski.it/react-native-form-validation-with-react-hook-form-usecontroller/

and figuring out how to use custom validation is from the react-hook-form docs

https://react-hook-form.com/advanced-usage#CustomHookwithResolver
