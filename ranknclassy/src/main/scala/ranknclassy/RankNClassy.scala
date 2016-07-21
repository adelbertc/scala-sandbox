package ranknclassy

import cats.data.Kleisli
import cats.implicits._

object RankNClassy {
  val f = new MonadHttp.Function[String] {
    def apply[F[_]: MonadHttp]: F[String] = for {
      _ <- MonadHttp[F].post("a", "a stuff")
      _ <- MonadHttp[F].post("b", "b stuff")
      a <- MonadHttp[F].get("a")
      b <- MonadHttp[F].get("b")
    } yield a ++ b
  }

  val foobar: Application[String] = Kleisli(service => service.runHttp(f))

  def mockHttpRequests[A](env: HttpEnv)(action: MonadHttp.Function[A]): IO[A] =
    action[MockHttp].runMockHttp.run(env)

  def runHttpRequest[A](action: MonadHttp.Function[A]): IO[A] = action[IO]

  def runApplicationTest[A](app: Application[A]): IO[A] = for {
    ref <- IORef.create(Map.empty[String, ByteString])
    a   <- app.run(new Services { def runHttp[B](f: MonadHttp.Function[B]): IO[B] = mockHttpRequests(ref)(f) })
  } yield a

  def runApplicationProd[A](app: Application[A]): IO[A] =
    app.run(new Services { def runHttp[B](f: MonadHttp.Function[B]): IO[B] = runHttpRequest(f) })

  val result = runApplicationTest(foobar).unsafePerformIO()
}
