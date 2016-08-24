package taglessStackSafety

import cats.{Apply, Monad, RecursiveTailRecM}
import cats.data.{NonEmptyList, State, Xor}
import cats.implicits._

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.{State => BenchState, _}

@BenchState(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ExprBenchmark {
  val ITERATIONS = 100000

  def largeExprReduceLeft[F[_]: Apply](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    NonEmptyList(0, List.range(1, ITERATIONS)).map(i => add(i) *> exists(i)).reduceLeft(_.map2(_)(_ && _))

  def largeExprReduceRight[F[_]: Apply](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    NonEmptyList(0, List.range(1, ITERATIONS)).map(i => add(i) *> exists(i)).reduceRight(_.map2Eval(_)(_ && _)).value

  def largeExprLoop[F[_]: Apply](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] = {
    def loop(i: Int, res: F[Boolean]): F[Boolean] =
      if (i == ITERATIONS) res
      else loop(i + 1, (add(i) *> exists(i)).map2(res)(_ && _))

    loop(1, add(0) *> exists(0))
  }

  def runTagless[A](set: TaglessSet[A]): A = set[State[Set[Int], ?]].runA(Set.empty[Int]).value

  def runFree[A](set: FreeSet.FreeSet[A]): A = set.foldMap(FreeSet.stateInterpreter).runA(Set.empty[Int]).value

  val safeTaglessSetReduceRight = largeExprReduceRight(TaglessSet.add, TaglessSet.exists)
  @Benchmark def taglessReduceRight = runTagless(safeTaglessSetReduceRight)

  val safeTaglessSetLoop = largeExprLoop(TaglessSet.add, TaglessSet.exists)
  @Benchmark def taglessLoop = runTagless(safeTaglessSetLoop)

  val freeSetReduceLeft = largeExprReduceLeft(FreeSet.add, FreeSet.exists)
  @Benchmark def freeReduceLeft = runFree(freeSetReduceLeft)

  val freeSetReduceRight = largeExprReduceRight(FreeSet.add, FreeSet.exists)
  @Benchmark def freeReduceRight = runFree(freeSetReduceRight)

  val freeSetLoop = largeExprLoop(FreeSet.add, FreeSet.exists)
  @Benchmark def freeLoop = runFree(freeSetLoop)
}
