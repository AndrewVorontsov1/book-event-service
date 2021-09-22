import cats.effect._
import cats.effect.unsafe.implicits.global

import scala.util.Random
object BookGenerator {
  val adj = List(
    "новый",
    "большой",
    "должен",
    "последний",
    "российский",
    "русский",
    "общий",
    "высокий",
    "хороший",
    "главный",
    "лучший",
    "маленький",
    "молодой"
  )

  val nom = List(
    "год",
    "человек",
    "день",
    "раз",
    "друг",
    "глаз",
    "вопрос",
    "дом",
    "мир",
    "случай",
    "ребенок",
    "конец",
    "вид"
  )
  val genBookName: IO[String] = IO(
    adj(Random.nextInt(adj.size)) + " " + nom(Random.nextInt(nom.size))
  )
  val getNameRun: String = genBookName.unsafeRunSync()
}
