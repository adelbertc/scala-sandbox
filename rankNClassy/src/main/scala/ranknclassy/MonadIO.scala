package rankNClassy

import cats.Monad

trait MonadIO[F[_]] extends Monad[F] {
  def liftIO[A](io: IO[A]): F[A]
}

object MonadIO {
  def apply[F[_]: MonadIO]: MonadIO[F] = implicitly[MonadIO[F]]
}
