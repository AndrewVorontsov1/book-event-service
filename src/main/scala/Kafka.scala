import Library.BookEvent
import cats.effect.kernel.Resource
import cats.effect.{Async, ExitCode, IO, IOApp}
import cats.implicits._
import fs2.Stream.awakeEvery
import fs2.kafka._
import org.apache.kafka.clients.admin.NewTopic

import java.util.UUID
import scala.concurrent.duration.DurationInt

object Kafka {
  def kafkaAdminClientResource[F[_]: Async](
      bootstrapServers: String
  ): Resource[F, KafkaAdminClient[F]] = {
    KafkaAdminClient.resource[F](AdminClientSettings(bootstrapServers))
  }

  def createTopic[F[_]: Async]: F[Unit] =
    kafkaAdminClientResource[F]("localhost:9092").use { client =>
      client.listTopics.names
        .map { set =>
          if (set.contains("book-event")) {
            ()
          } else {
            client.createTopic(new NewTopic("book-events", 1, 1.toShort))
          }
        }
    }

  val producerSettings: ProducerSettings[IO, String, Array[Byte]] =
    ProducerSettings[IO, String, Array[Byte]]
      .withBootstrapServers("localhost:9092")

  def record(
      bookId: String,
      bookName: String
  ): ProducerRecord[String, Array[Byte]] =
    ProducerRecord(
      topic = "book-events",
      key = bookId,
      value = BookEvent(
        id = bookId,
        name = bookName,
        year = bookId
      ).toByteArray
    )

  val produce = awakeEvery[IO](1.seconds)
    .evalMap(_ => IO.delay(UUID.randomUUID().toString))
    .flatMap(bookId =>
      fs2.Stream
        .eval(BookGenerator.genBookName)
        .map(name => ProducerRecords.one(record(bookId, name)))
    )
    .flatTap { rec =>
      KafkaProducer.stream(producerSettings).evalMap(_.produce(rec).flatten)
    }
    .compile
    .drain
}
