
import java.nio.charset.Charset
import java.nio.file.Paths

import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser
import com.nulabinc.backlog.c2b.core.domain.parser.{CSVParseError, CSVRecordParser}
import org.apache.commons.csv.{CSVFormat, CSVParser}
import monix.eval.Task
import monix.reactive.{Consumer, Observable}

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import monix.execution.Scheduler.Implicits.global

object Main extends App {

  println("Start")

  val concurrentProcesses = 10
  val csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()

  val csvFiles = Paths.get("./modules/core/src/test/scala").toFile.listFiles().filter(_.getName.endsWith(".csv"))

  def printResult(data: Either[CSVParseError[CybozuUser], CybozuUser]): Task[Unit] = Task {
    data match {
      case Right(user) => println(user)
      case Left(ex) => println(ex.toString)
    }
  }

  val printingResults: Consumer[Either[CSVParseError[CybozuUser], CybozuUser], Unit] =
    Consumer.foreachParallelAsync(concurrentProcesses)(printResult)

  val a = Observable
    .fromIterable(csvFiles)
    .mapAsync(csvFiles.length) { file =>
      Observable.fromIterator(CSVParser.parse(file, Charset.forName("UTF-8"), csvFormat).iterator().asScala)
        .map(CSVRecordParser.user)
        .consumeWith(printingResults)
    }
    .completedL
    .runAsync

  Await.ready(a, Duration.Inf)
  println("Finish")
}
