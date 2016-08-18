package taglessStackSafety

import cats.{Apply, Monad, RecursiveTailRecM}
import cats.data.{NonEmptyList, State, Xor}
import cats.implicits._

object BenchmarkApp extends App {
  def largeExpr[F[_]: Apply](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    NonEmptyList(0, List.range(1, 10000)).map(i => add(i) *> exists(i)).reduceLeft(_.map2(_)(_ && _))

  def tailRecLargeExpr[F[_]: Monad: RecursiveTailRecM](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    Monad[F].tailRecM((10000, true)) {
      case (n, result) =>
        if (n == 0) Monad[F].pure(Xor.right(result))
        else Monad[F].map(add(n) *> exists(n))(b => Xor.left((n - 1, b)))
    }

  val safeTaglessSet = tailRecLargeExpr(TaglessSet.add, TaglessSet.exists)
  val safeTaglessResult = safeTaglessSet[State[Set[Int], ?]].runA(Set.empty[Int]).value
  println(safeTaglessResult)

  // val freeSet = largeExpr(FreeSet.add, FreeSet.exists)
  // val freeResult = freeSet.foldMap(FreeSet.stateInterpreter).runA(Set.empty[Int]).value

  // Blows stack
  // val taglessSet = largeExpr(TaglessSet.add, TaglessSet.exists)
  // val taglessResult = taglessSet[State[Set[Int], ?]].runA(Set.empty[Int]).value
}
