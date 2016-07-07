package taglessStackSafety

import cats.Monad
import cats.data.State

trait MonadSet[F[_]] extends Monad[F] {
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
  }
}

trait TaglessSet[A] {
  def run[F[_]: MonadSet]: F[A]
}

object TaglessSet {
  implicit val instance: Monad[TaglessSet] = new Monad[TaglessSet] {
    def pure[A](x: A): TaglessSet[A] = new TaglessSet[A] {
      def run[F[_]: MonadSet]: F[A] = MonadSet[F].pure(x)
    }

    def flatMap[A, B](fa: TaglessSet[A])(f: A => TaglessSet[B]): TaglessSet[B] = new TaglessSet[B] {
      def run[F[_]: MonadSet]: F[B] = MonadSet[F].flatMap(fa.run(MonadSet[F]))(a => f(a).run(MonadSet[F]))
    }
  }

  def add(int: Int): TaglessSet[Unit] = new TaglessSet[Unit] {
    def run[F[_]: MonadSet]: F[Unit] = MonadSet[F].add(int)
  }

  def exists(int: Int): TaglessSet[Boolean] = new TaglessSet[Boolean] {
    def run[F[_]: MonadSet]: F[Boolean] = MonadSet[F].exists(int)
  }
}
