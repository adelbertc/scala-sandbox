package ranknclassy.interpret

import cats.{Applicative, Functor, Monad, MonadReader}
import cats.free.Free
import cats.implicits._
import ranknclassy.{IO, MonadIO}

trait Category[F[_[_[_]], _[_[_]]]] {
  def id[TC[_[_]]]: F[TC, TC]

  def compose[TC[_[_]], TD[_[_]], TE[_[_]]](f: F[TD, TE], g: F[TC, TD]): F[TC, TE]
}

object Category {
  def apply[F[_[_[_]], _[_[_]]]: Category]: Category[F] = implicitly[Category[F]]

  implicit class CategorySyntax[F[_[_[_]], _[_[_]]], TD[_[_]], TE[_[_]]](val f: F[TD, TE]) extends AnyVal {
    def compose[TC[_[_]]](other: F[TC, TD])(implicit F: Category[F]): F[TC, TE] = F.compose(f, other)
  }
}

trait Interpret[C[_[_]], D[_[_]]] {
  def apply[N[_]: D, A](f: Interpret.Constrained[C, A]): N[A]
}

object Interpret {
  trait Constrained[TC[_[_]], A] {
    def apply[F[_]: TC]: F[A]
  }

  implicit def categoryInterpret: Category[Interpret] =
    new Category[Interpret] {
      def id[TC[_[_]]]: Interpret[TC, TC] = new Interpret[TC, TC] {
        def apply[N[_]: TC, A](f: Constrained[TC, A]): N[A] = f[N]
      }

      def compose[TC[_[_]], TD[_[_]], TE[_[_]]](f: Interpret[TD, TE], g: Interpret[TC, TD]): Interpret[TC, TE] =
        new Interpret[TC, TE] {
          def apply[N[_]: TE, A](h: Constrained[TC, A]): N[A] = {
            val gh = new Constrained[TD, A] {
              def apply[F[_]: TD]: F[A] = g(h)
            }
            f(gh)
          }
        }
    }
}

trait MonadHttp[F[_]] extends Monad[F] {
  def httpGet(s: String): F[String]
}

object MonadHttp {
  def apply[F[_]: MonadHttp]: MonadHttp[F] = implicitly[MonadHttp[F]]
}

final case class HttpApp[A](runHttpApp: IO[A]) extends AnyVal

object HttpApp {
  implicit val instancesHttpApp: MonadHttp[HttpApp] with MonadIO[HttpApp] = new MonadHttp[HttpApp] with MonadIO[HttpApp] {
    def httpGet(s: String): HttpApp[String] = HttpApp(IO("[]"))
    def flatMap[A, B](fa: HttpApp[A])(f: A => HttpApp[B]): HttpApp[B] =
      HttpApp(fa.runHttpApp.flatMap(a => f(a).runHttpApp))
    def pure[A](x: A): HttpApp[A] = HttpApp(IO(x))
    def liftIO[A](io: IO[A]): HttpApp[A] = HttpApp(io)
  }
}

final case class MockHttp[F[_], A](runMockHttp: F[A]) extends AnyVal

object MockHttp {
  implicit def monadIOMockHttp[F[_]: MonadIO]: MonadIO[MockHttp[F, ?]] = new MonadIO[MockHttp[F, ?]] {
    def flatMap[A, B](fa: MockHttp[F, A])(f: A => MockHttp[F, B]): MockHttp[F, B] =
      MockHttp(fa.runMockHttp.flatMap(a => f(a).runMockHttp))
    def pure[A](x: A): MockHttp[F, A] = MockHttp(Monad[F].pure(x))
    def liftIO[A](io: IO[A]): MockHttp[F, A] = MockHttp(MonadIO[F].liftIO(io))
  }

  implicit def monadReaderMockHttp[F[_], R](implicit R: MonadReader[F, R]): MonadReader[MockHttp[F, ?], R] =
    new MonadReader[MockHttp[F, ?], R] {
      def flatMap[A, B](fa: MockHttp[F, A])(f: A => MockHttp[F, B]): MockHttp[F, B] =
        MockHttp(fa.runMockHttp.flatMap(a => f(a).runMockHttp))
      def pure[A](x: A): MockHttp[F, A] = MockHttp(Monad[F].pure(x))
      def ask: MockHttp[F, R] = MockHttp(R.ask)
      def local[A](f: R => R)(fa: MockHttp[F, A]): MockHttp[F, A] = MockHttp(R.local(f)(fa.runMockHttp))
    }

  implicit def monadHttpMockHttp[F[_]](implicit R: MonadReader[F, String]): MonadHttp[MockHttp[F, ?]] =
    new MonadHttp[MockHttp[F, ?]] {
      def flatMap[A, B](fa: MockHttp[F, A])(f: A => MockHttp[F, B]): MockHttp[F, B] =
        MockHttp(fa.runMockHttp.flatMap(a => f(a).runMockHttp))
      def pure[A](x: A): MockHttp[F, A] = MockHttp(Monad[F].pure(x))
      def httpGet(s: String): MockHttp[F, String] = MockHttp(R.ask)
    }
}

trait MonadRestApi[F[_]] extends Monad[F] {
  def getUserIds: F[List[Int]]
}

object MonadRestApi {
  def apply[F[_]: MonadRestApi]: MonadRestApi[F] = implicitly[MonadRestApi[F]]
}

final case class RestApiF[A](getUsers: List[Int] => A) extends AnyVal

object RestApi {
  type RestApi[A] = Free[RestApiF, A]

  implicit val functorRestApiF: Functor[RestApiF] = new Functor[RestApiF] {
    def map[A, B](fa: RestApiF[A])(f: A => B): RestApiF[B] = RestApiF(fa.getUsers andThen f)
  }

  implicit val monadRestApiRestApi: MonadRestApi[RestApi] = new MonadRestApi[RestApi] {
    def getUserIds: RestApi[List[Int]] = Free.liftF(RestApiF(identity))
    def flatMap[A, B](fa: RestApi[A])(f: A => RestApi[B]): RestApi[B] = fa.flatMap(f)
    def pure[A](a: A): RestApi[A] = Free.pure(a)
  }
}

object InterpretApp {
  val runIO: Interpret[MonadHttp, MonadIO] = new Interpret[MonadHttp, MonadIO] {
    def apply[N[_]: MonadIO, A](f: Interpret.Constrained[MonadHttp, A]): N[A] = {
      val httpApp = f[HttpApp]
      MonadIO[N].liftIO(httpApp.runHttpApp)
    }
  }

  val runMock: Interpret[MonadHttp, MonadReader[?[_], String]] = new Interpret[MonadHttp, MonadReader[?[_], String]] {
    def apply[N[_], A](f: Interpret.Constrained[MonadHttp, A])(implicit R: MonadReader[N, String]): N[A] =
     f[MockHttp[N, ?]].runMockHttp
  }

  def iterA[F[_]: Functor, P[_]: Applicative, A](f: F[P[A]] => P[A])(free: Free[F, A]): P[A] = {
    def puref(a: A): P[A] = Applicative[P].pure(a)
    def freef(s: F[Free[F, A]]): P[A] = f(s.map(iterA(f)))
    free.fold(puref, freef)
  }

  val runRestApi: Interpret[MonadRestApi, MonadHttp] = new Interpret[MonadRestApi, MonadHttp] {
    // Totally completely safe way to parse lists of form [1,2,3]
    def read(s: String): List[Int] = {
      val arr = s.drop(1).dropRight(1).split(",").filter(_.nonEmpty)
      if (arr.isEmpty) List.empty[Int] else arr.toList.map(_.toInt)
    }

    def apply[N[_]: MonadHttp, A](f: Interpret.Constrained[MonadRestApi, A]): N[A] = {
      import RestApi._
      def go(f: RestApiF[N[A]]): N[A] = for {
        response <- MonadHttp[N].httpGet("url")
        a        <- f.getUsers(read(response))
      } yield a
      iterA(go)(f[RestApi.RestApi])
    }
  }

  import Category.CategorySyntax

  val runApplication: Interpret[MonadRestApi, MonadIO] = runIO compose runRestApi

  val mockApplication: Interpret[MonadRestApi, MonadReader[?[_], String]] =
    Category[Interpret].compose[MonadRestApi, MonadHttp, MonadReader[?[_], String]](runMock, runRestApi)

  val f = new Interpret.Constrained[MonadRestApi, List[Int]] {
    def apply[M[_]: MonadRestApi]: M[List[Int]] = MonadRestApi[M].getUserIds
  }

  val app: HttpApp[List[Int]] = runApplication[HttpApp, List[Int]](f)

  // omg if we don't annotate the types it automagically infers String => List[Int]
  val mock: MockHttp[String => ?, List[Int]] = mockApplication[MockHttp[String => ?, ?], List[Int]](f)
}
