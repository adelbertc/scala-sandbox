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

  def largeExpr[F[_]: Apply](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    NonEmptyList(0, List.range(1, ITERATIONS - 1)).map(i => add(i) *> exists(i)).reduceLeft(_.map2(_)(_ && _))

  def tailRecLargeExpr[F[_]: Monad: RecursiveTailRecM](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    Monad[F].tailRecM((ITERATIONS, true)) {
      case (n, result) =>
        if (n == 0) Monad[F].pure(Xor.right(result))
        else Monad[F].map(add(n) *> exists(n))(b => Xor.left((n - 1, b)))
    }

  @Benchmark
  def tagless = {
    val safeTaglessSet = tailRecLargeExpr(TaglessSet.add, TaglessSet.exists)
    safeTaglessSet[State[Set[Int], ?]].runA(Set.empty[Int]).value
  }

  @Benchmark
  def free = {
    val freeSet = largeExpr(FreeSet.add, FreeSet.exists)
    freeSet.foldMap(FreeSet.stateInterpreter).runA(Set.empty[Int]).value
  }

  // Blows stack
  // val taglessSet = largeExpr(TaglessSet.add, TaglessSet.exists)
  // val taglessResult = taglessSet[State[Set[Int], ?]].runA(Set.empty[Int]).value
}
