This repo demonstrates how to use [malli](https://github.com/metosin/malli) schemas to validate forms with [react-hook-form](https://react-hook-form.com/get-started)

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
