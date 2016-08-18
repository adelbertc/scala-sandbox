# scala-sandbox
Random Scala thought experiments and miscellany.

## Modules
### ranknclassy
Translation of "Rank 'n Classy Limited Effects from Haskell to Scala.

* [Rank 'n Classy Limited Effects](http://www.parsonsmatt.org/2016/07/14/rank_n_classy_limited_effects.html)

### tagless-stacksafety
Explorations into making tagless final EDSLs stack safe.

This project comes with a toy benchmark that can be run with:

```bash
$ sbt "taglessStackSafety/jmh:run -i 10 -wi 10 -f 2 -t 1 taglessStackSafety.ExprBenchmark"
```

Below are results from running on my local machine:

```
[info] Benchmark                                  Mode  Cnt  Score   Error  Units
[info] taglessStackSafety.ExprBenchmark.free     thrpt   20  5.584 ± 0.163  ops/s
[info] taglessStackSafety.ExprBenchmark.tagless  thrpt   20  9.210 ± 0.141  ops/s
```

* [Alternatives to GADTs in Scala](https://pchiusano.github.io/2014-05-20/scala-gadts.html)
* [Tagless-Final Style](http://okmij.org/ftp/tagless-final/)

### License
Code is provided under the Apache 2.0 license available at https://www.apache.org/licenses/LICENSE-2.0, as well as the
LICENSE file.
