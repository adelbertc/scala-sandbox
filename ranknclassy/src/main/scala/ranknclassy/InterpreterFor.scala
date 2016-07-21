package ranknclassy

import cats.data.Kleisli

trait InterpreterFor[G[_], Eff[_[_]]] {
  trait Function[A] {
    def apply[F[_]: Eff]: F[A]
  }
  def apply[A](f: Function[A]): G[A]
}

object InterpreterFor {
  final case class Services[Eff[_]](runHttp: Eff InterpreterFor MonadHttp) extends AnyVal

  trait Application[A] {
    def apply[M[_]]: Kleisli[M, Services[M], A]
  }
}
