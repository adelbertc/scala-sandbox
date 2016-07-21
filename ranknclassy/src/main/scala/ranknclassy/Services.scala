package ranknclassy

trait Services {
  def runHttp[A](f: MonadHttp.Function[A]): IO[A]
}
