package taglessStackSafety

import cats.{~>, Id}
import cats.data.State
import cats.free.Free

sealed abstract class FreeSetF[A] extends Product with Serializable

object FreeSetF {
  final case class Add(int: Int) extends FreeSetF[Unit]
  final case class Exists(int: Int) extends FreeSetF[Boolean]
}

object FreeSet {
  import FreeSetF._

  type FreeSet[A] = Free[FreeSetF, A]

  def add(int: Int): FreeSet[Unit] = Free.liftF(Add(int))
  def exists(int: Int): FreeSet[Boolean] = Free.liftF(Exists(int))

  val stateInterpreter = new (FreeSetF ~> State[Set[Int], ?]) {
    def apply[A](fa: FreeSetF[A]): State[Set[Int], A] = fa match {
      case Add(i)    => State.modify(_ + i)
      case Exists(i) => State.inspect(_.contains(i))
    }
  }

  def mutableInterpreter = new (FreeSetF ~> Id) {
    var mutableSet = scala.collection.mutable.Set.empty[Int]
    def apply[A](fa: FreeSetF[A]): A = fa match {
      case Add(i)    =>
        mutableSet += i
        ()
      case Exists(i) => mutableSet.contains(i)
    }
  }
}
