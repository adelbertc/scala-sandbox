package rankNClassy

import cats.MonadReader
import cats.data.Kleisli
import cats.implicits._
import io.circe.Encoder
import io.circe.syntax._

final case class MockHttp[A](runMockHttp: Kleisli[IO, HttpEnv, A]) extends AnyVal

object MockHttp {
  implicit val instancesForMockHttp: MonadHttp[MockHttp] with MonadReader[MockHttp, HttpEnv] with MonadIO[MockHttp] =
    new MonadHttp[MockHttp] with MonadReader[MockHttp, HttpEnv] with MonadIO[MockHttp] {
      def pure[A](x: A): MockHttp[A] = MockHttp(Kleisli.pure(x))

      def flatMap[A, B](fa: MockHttp[A])(f: A => MockHttp[B]): MockHttp[B] =
        MockHttp(fa.runMockHttp.flatMap(a => f(a).runMockHttp))

      def get(url: Url): MockHttp[ByteString] =
        ask.flatMap(env => liftIO(env.read)).map(state => state(url))

      def post[A: Encoder](url: Url,a: A): MockHttp[ByteString] = for {
        ref <- ask
        state <- liftIO(ref.read)
        _ <- liftIO(ref.write(state + ((url, a.asJson.noSpaces))))
      } yield "200 OK"

      def liftIO[A](io: IO[A]): MockHttp[A] = MockHttp(Kleisli(_ => io))

      def ask: MockHttp[HttpEnv] = MockHttp(Kleisli.ask[IO, HttpEnv])

      def local[A](f: HttpEnv => HttpEnv)(fa: MockHttp[A]): MockHttp[A] = MockHttp(fa.runMockHttp.local(f))
    }
}
