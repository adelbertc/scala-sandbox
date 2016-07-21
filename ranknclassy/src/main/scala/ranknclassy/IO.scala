package ranknclassy

import cats.{Eval, Monad}

trait IO[A] {
  def unsafePerformIO(): A
}

object IO {
  def apply[A](a: => A): IO[A] = Monad[IO].pureEval(Eval.always(a))

  implicit val monadIO: Monad[IO] = new Monad[IO] {
    def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] =
      f(fa.unsafePerformIO())

    def pure[A](x: A): IO[A] = new IO[A] {
      def unsafePerformIO(): A = x
    }

    override def pureEval[A](x: Eval[A]): IO[A] = new IO[A] {
      def unsafePerformIO(): A = x.value
    }
  }
}
