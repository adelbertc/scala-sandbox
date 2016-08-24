package rankNClassy

import cats.free.Free
import io.circe.Encoder

sealed abstract class HttpF[A] extends Product with Serializable

object HttpF {
  final case class Get(url: Url) extends HttpF[ByteString]
  final case class Post[A: Encoder](url: Url, a: A) extends HttpF[ByteString]

  type FreeHttp[A] = Free[HttpF, A]

  implicit def monadHttpFreeHttp: MonadHttp[FreeHttp] = new MonadHttp[FreeHttp] {
    def get(url: Url): FreeHttp[ByteString] = Free.liftF(Get(url))

    def post[A: Encoder](url: Url, a: A): FreeHttp[ByteString] = Free.liftF[HttpF, ByteString](Post(url, a)(Encoder[A]))

    def pure[A](x: A): FreeHttp[A] = Free.pure(x)

    def flatMap[A, B](fa: FreeHttp[A])(f: A => FreeHttp[B]): FreeHttp[B] = fa.flatMap(f)
  }

  def reify[A](f: MonadHttp.Function[A]): FreeHttp[A] = f[FreeHttp]
}
