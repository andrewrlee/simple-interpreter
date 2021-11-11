# klox
A Kotlin JVM/JS implementation of Lox from Crafting Interpreters (by `@munificentbob`)

## To generate AST

Run  `src/jvmMain/kotlin/klox/tools/GenerateAst.kt`

## To run via CLI

Run `src/jvmMain/kotlin/klox/LoxWrapper.kt` to see usage.

## To run via a browser

Generate language script:

`./gradlew clean jsBrowserDistribution`

Include language script in to page:
```
  <script src="./build/distributions/klox.js"></script>
```

Create a new instance of a Lox interpreter and execute lox programs using `run()`
```js
    const handler = {
        println: (message) => {
            console.log(message);
        },
        onError: (message) => {
            console.error(message)
        }
    }

    const lox = new klox.klox.Lox(handler);

    lox.run("print 1 + 2");
    
    // Clear error flag if necessary before running again
    lox.reset();
```