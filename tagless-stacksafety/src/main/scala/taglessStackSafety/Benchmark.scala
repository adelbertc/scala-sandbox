package taglessStackSafety

import cats.Apply
import cats.data.{NonEmptyList, State}
import cats.implicits._

object BenchmarkApp extends App {
  def largeExpr[F[_]: Apply](add: Int => F[Unit], exists: Int => F[Boolean]): F[Boolean] =
    NonEmptyList(0, List.range(1, 5000)).map(i => add(i) *> exists(i)).reduceLeft(_.map2(_)(_ && _))

  // val freeSet = largeExpr(FreeSet.add, FreeSet.exists)
  // val freeResult = freeSet.foldMap(FreeSet.stateInterpreter).runA(Set.empty[Int]).value
  // println(s"freeSetResult: ${freeResult}")

  val safeTaglessSet = largeExpr(SafeTaglessSet.add, SafeTaglessSet.exists)
  val safeTaglessResult = safeTaglessSet.run[State[Set[Int], ?]].flatMap(_.runA(Set.empty[Int])).value
  println(s"safeTaglessSet: ${safeTaglessResult}")

  // Blows stack
  // val badFreeResult = freeSet.foldMap(FreeSet.mutableInterpreter)

  // val taglessSet = largeExpr[TaglessSet](TaglessSet.add, TaglessSet.exists)
  // val taglessResult = taglessSet.run[State[Set[Int], ?]]
}
