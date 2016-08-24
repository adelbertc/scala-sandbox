# scala-sandbox
Random Scala thought experiments and miscellany.

## Modules
### rankNClassy
Translation of "Rank 'n Classy Limited Effects from Haskell to Scala.

* [Rank 'n Classy Limited Effects](http://www.parsonsmatt.org/2016/07/14/rank_n_classy_limited_effects.html)

### taglessStackSafety
Explorations into making tagless final EDSLs stack safe.

* [Alternatives to GADTs in Scala](https://pchiusano.github.io/2014-05-20/scala-gadts.html)
* [Tagless-Final Style](http://okmij.org/ftp/tagless-final/)

This project comes with a toy benchmark that can be run with:

```bash
$ sbt "taglessStackSafety/jmh:run -i 10 -wi 10 -f 2 -t 1 taglessStackSafety.ExprBenchmark"
```

Below are results from running on my local machine:

```
[info] Benchmark                                             Mode  Cnt  Score   Error  Units
[info] taglessStackSafety.ExprBenchmark.freeLoop            thrpt   20  5.387 ± 0.144  ops/s
[info] taglessStackSafety.ExprBenchmark.freeReduceLeft      thrpt   20  5.819 ± 0.116  ops/s
[info] taglessStackSafety.ExprBenchmark.freeReduceRight     thrpt   20  5.284 ± 0.159  ops/s
[info] taglessStackSafety.ExprBenchmark.taglessLoop         thrpt   20  5.131 ± 0.185  ops/s
[info] taglessStackSafety.ExprBenchmark.taglessReduceRight  thrpt   20  5.194 ± 0.245  ops/s
```

While writing these benchmarks I've realized how finicky stack safety can get. The benchmark for finally
tagless only uses `reduceRight` - with `reduceLeft` it will blow the stack due to the evaluation
differences of the two (non-strict vs. strict, respectively). Contrast with the `Free` algebra which does
fine with both since stack safety is built in to the structure.

### License
Code is provided under the Apache 2.0 license available at https://www.apache.org/licenses/LICENSE-2.0, as well as the
LICENSE file.
