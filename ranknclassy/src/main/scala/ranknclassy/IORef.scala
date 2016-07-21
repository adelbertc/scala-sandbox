package ranknclassy

sealed abstract class IORef[A] {
  protected var value: A
  def read: IO[A] = IO { value }
  def write(a: A): IO[Unit] = IO { value = a }
}

object IORef {
  def create[A](a: A): IO[IORef[A]] = IO {
    new IORef[A] {
      protected var value = a
    }
  }
}
