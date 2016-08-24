import cats.data.Kleisli

package object rankNClassy {
  type ByteString = String
  type Url = String

  type HttpState = Map[String, ByteString]
  type HttpEnv = IORef[HttpState]

  type Application[A] = Kleisli[IO, Services, A]
}
