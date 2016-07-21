package ranknclassy

import cats.{Eval, Monad}
import io.circe.Encoder

trait MonadHttp[F[_]] extends Monad[F] {
  def get(url: Url): F[ByteString]

  def post[A: Encoder](url: Url, a: A): F[ByteString]
}

object MonadHttp {
  def apply[F[_]: MonadHttp]: MonadHttp[F] = implicitly[MonadHttp[F]]

  implicit def monadHttpIO: MonadHttp[IO] = new MonadHttp[IO] {
    def get(url: Url): IO[ByteString] = ???

    def post[A: Encoder](url: Url, a: A): IO[ByteString] = ???

    def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] =
      f(fa.unsafePerformIO())

    def pure[A](x: A): IO[A] = new IO[A] {
      def unsafePerformIO(): A = x
    }

    override def pureEval[A](x: Eval[A]): IO[A] = new IO[A] {
      def unsafePerformIO(): A = x.value
    }
  }

  trait Function[A] {
    def apply[F[_]: MonadHttp]: F[A]
  }
}
