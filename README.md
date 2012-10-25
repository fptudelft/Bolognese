# Bolognese #

## Build & Run ##

```sh
$ cd Bolognese
$ ./sbt
> container:start
> ~ ;copy-resources;aux-compile
```
Now open the [root page](http://localhost:8080/) in your browser.

## "Continuations" related Erros in Eclipse ##

Follow these instructions: http://scala-ide.org/docs/tutorials/continuations-plugin/index.html

Also, create a .project file specific for your setup by running ./sbt and then `eclipse'.
Do this every time you add another library.
