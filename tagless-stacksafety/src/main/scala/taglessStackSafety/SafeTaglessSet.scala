package taglessStackSafety

import cats.{Eval, Monad}
import cats.data.State
import cats.implicits._

trait SafeMonadSet[F[_]] extends Monad[F] {
  def add(int: Int): Eval[F[Unit]]
  def exists(int: Int): Eval[F[Boolean]]
}

object SafeMonadSet {
  def apply[F[_]](implicit F: SafeMonadSet[F]): SafeMonadSet[F] = F

  implicit val stateInterpreter: SafeMonadSet[State[Set[Int], ?]] = new SafeMonadSet[State[Set[Int], ?]] {
    def add(int: Int): Eval[State[Set[Int], Unit]] = Eval.now(State.modify(_ + int))
    def exists(int: Int): Eval[State[Set[Int], Boolean]] = Eval.now(State.inspect(_.contains(int)))
    def pure[A](x: A): State[Set[Int], A] = State.pure(x)
    def flatMap[A, B](fa: State[Set[Int], A])(f: A => State[Set[Int], B]): State[Set[Int], B] = fa.flatMap(f)
  }
}

trait SafeTaglessSet[A] {
  def run[F[_]: SafeMonadSet]: Eval[F[A]]
}

object SafeTaglessSet {
  implicit val instance: Monad[SafeTaglessSet] = new Monad[SafeTaglessSet] {
    def pure[A](x: A): SafeTaglessSet[A] = new SafeTaglessSet[A] {
      def run[F[_]: SafeMonadSet]: Eval[F[A]] = Eval.now(SafeMonadSet[F].pure(x))
    }

    def flatMap[A, B](fa: SafeTaglessSet[A])(f: A => SafeTaglessSet[B]): SafeTaglessSet[B] = new SafeTaglessSet[B] {
      def run[F[_]: SafeMonadSet]: Eval[F[B]] = {
        // evalFa.flatMap { (fa: F[A]) =>
        //   fa.flatMap { (a: A) =>
        //     f(a).run[F].value
        //   }
        // }
        ???
      }
    }
  }

  def add(int: Int): SafeTaglessSet[Unit] = new SafeTaglessSet[Unit] {
    def run[F[_]: SafeMonadSet]: Eval[F[Unit]] = SafeMonadSet[F].add(int)
  }

  def exists(int: Int): SafeTaglessSet[Boolean] = new SafeTaglessSet[Boolean] {
    def run[F[_]: SafeMonadSet]: Eval[F[Boolean]] = SafeMonadSet[F].exists(int)
  }
}
