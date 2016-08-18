package taglessStackSafety

import cats.{Eval, Monad, RecursiveTailRecM}
import cats.data.{State, StateT, Xor}

trait MonadSet[F[_]] extends Monad[F] with RecursiveTailRecM[F] {
  def add(int: Int): F[Unit]
  def exists(int: Int): F[Boolean]
}

object MonadSet {
  def apply[F[_]](implicit F: MonadSet[F]): MonadSet[F] = F

  implicit val stateInterpreter: MonadSet[State[Set[Int], ?]] = new MonadSet[State[Set[Int], ?]] {
    def add(int: Int): State[Set[Int], Unit] = State.modify(_ + int)
    def exists(int: Int): State[Set[Int], Boolean] = State.inspect(_.contains(int))
    def pure[A](x: A): State[Set[Int], A] = State.pure(x)
    def flatMap[A, B](fa: State[Set[Int], A])(f: A => State[Set[Int], B]): State[Set[Int], B] = fa.flatMap(f)
    def tailRecM[A, B](a: A)(f: A => State[Set[Int], Xor[A, B]]): State[Set[Int], B] =
      StateT.catsDataMonadForStateT[Eval, Set[Int]].tailRecM(a)(f)
  }

  implicit val evalInterpreter: MonadSet[Eval] = new MonadSet[Eval] {
    def add(int: Int): Eval[Unit] = Eval.now(())
    def exists(int: Int): Eval[Boolean] = Eval.now(true)
    def pure[A](x: A): Eval[A] = Eval.now(x)
    def flatMap[A, B](fa: Eval[A])(f: A => Eval[B]): Eval[B] = fa.flatMap(f)
    def tailRecM[A, B](a: A)(f: A => Eval[Xor[A, B]]): Eval[B] = defaultTailRecM(a)(f)
  }
}

trait TaglessSet[A] {
  def apply[F[_]: MonadSet]: F[A]
}

object TaglessSet {
  implicit val instance: Monad[TaglessSet] with RecursiveTailRecM[TaglessSet] =
    new Monad[TaglessSet] with RecursiveTailRecM[TaglessSet] {
      def pure[A](x: A): TaglessSet[A] = new TaglessSet[A] {
        def apply[F[_]: MonadSet]: F[A] = MonadSet[F].pure(x)
      }

      def flatMap[A, B](fa: TaglessSet[A])(f: A => TaglessSet[B]): TaglessSet[B] = new TaglessSet[B] {
        def apply[F[_]: MonadSet]: F[B] = MonadSet[F].flatMap(fa[F])(a => f(a)[F])
      }

      def tailRecM[A, B](a: A)(f: A => TaglessSet[Xor[A, B]]): TaglessSet[B] = new TaglessSet[B] {
        def apply[F[_]: MonadSet]: F[B] = MonadSet[F].tailRecM(a)(a => f(a)[F])
      }
    }

  def add(int: Int): TaglessSet[Unit] = new TaglessSet[Unit] {
    def apply[F[_]: MonadSet]: F[Unit] = MonadSet[F].add(int)
  }

  def exists(int: Int): TaglessSet[Boolean] = new TaglessSet[Boolean] {
    def apply[F[_]: MonadSet]: F[Boolean] = MonadSet[F].exists(int)
  }
}
