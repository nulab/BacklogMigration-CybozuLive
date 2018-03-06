import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}

import com.nulabinc.backlog.c2b.core.domain.model.CybozuUser
import com.nulabinc.backlog.c2b.parser.CsvRecordParser
import org.apache.commons.csv.{CSVFormat, CSVParser}
import monix.eval.Task
import monix.reactive.{Observable, OverflowStrategy}

import scala.collection.JavaConverters._
import monix.nio.file._
import monix.reactive.observables.ObservableLike.Transformer

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object Main extends App {

  println("Starting generation")

  val numberOfFiles = sources.length
  val numberOfPairs = 1
  val concurrentFiles = 2

  val parser = CSVParser.parse(sources.head, CSVFormat.DEFAULT)

  val s1 = Observable.fromIterator(parser.iterator().asScala)
    .map(CsvRecordParser.user)

//  val s2 = Observable.fromIterator(parser.iterator().asScala)
//    .map(CsvRecordParser.user)
//    .map { cyborgUser =>
//
//    }

//  Observable.merge(s1, s2)
  /*
  Observable
    .range(1, numberOfFiles)
    .bufferTumbling(concurrentFiles)
    .flatMap { fs =>
      Observable.merge(fs.map(f => Observable.now(f).transform(processSingleFile)): _*)
    }
    .consumeWith(storeReadings)
    .doOnFinish { _ =>
      println(s"Import finished")
      Task.unit
    }
    .onErrorHandle(e => println("Import failed", e))
    .runAsync*/


  /*val parseFile: Transformer[String, CybozuUser] = _.concatMap { file =>
    Observable.fromLinesReader(new BufferedReader(new InputStreamReader(new FileInputStream(file))))
      .drop(1)
      .transform(mapAsyncOrdered(nonIOParallelism)(parseLine))
  }*/

  //val processSingleFile: Transformer[String, CybozuUser] = _.transform(parseFile).transform(computeAverage)

  def sources = Seq(
    """""姓","名","よみがな姓","よみがな名","メールアドレス"
      |"Shoma","Nishitaten","","","shoma.nishitateno@nulab-inc.com"
      |"内田","優一","うちだ","ゆういち","uchida@nulab-inc.com"
      |""".stripMargin,
    """""姓","名","よみがな姓","よみがな名","メールアドレス"
      |"Aaa","Aaaaaa","aaa","aaaa","aaa@nulab-inc.com"
      |"Bbb","Bbbbbb","bbb","bbbb","bbbb@nulab-inc.com"
      |""".stripMargin,
    """""姓","名","よみがな姓","よみがな名","メールアドレス"
      |"Ccc","Cccc","ccc","cccc","ccc@nulab-inc.com"
      |"Ddd","Dddd","ddd","dddd","ddd@nulab-inc.com"
      |""".stripMargin,
  )
}

object Main2 extends App {

  val parser = CSVParser.parse(data, CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord())

  parser.getRecords.asScala.foreach { record =>
    try {
      println(record.get(0) + " " + record.get(1))
    } catch {
      case ex: Throwable => println(record.toString)
    }
//    println(record)
  }

  def data =
    """"ID","タイトル","本文","作成者","作成日時","更新者","更新日時","ステータス","優先度","担当者","期日","コメント"
      |"1:2929320","あとからカテゴリをリネーム","","Shoma Nishitaten","2018/2/28 09:57","Shoma Nishitaten","2018/2/28 09:57","未着手","B","","2018/2/28",""
      |"1:2929315","あとからカテゴリを削除","","Shoma Nishitaten","2018/2/28 09:54","Shoma Nishitaten","2018/2/28 09:54","未着手","B","","2018/2/28",""
      |"1:2929300","複数担当者","これどうなるの","Shoma Nishitaten","2018/2/28 09:48","Shoma Nishitaten","2018/2/28 09:48","保留","B","Shoma Nishitaten,内田 優一","2018/2/28",""
      |"1:2929277","コメントテスト","","Shoma Nishitaten","2018/2/28 09:44","Shoma Nishitaten","2018/2/28 09:44","未着手","B","","2018/2/28","--------------------------------------------------
      |3: Shoma Nishitaten 2018/2/28 (水) 09:44
      |
      |3
      |
      |--------------------------------------------------
      |2: Shoma Nishitaten 2018/2/28 (水) 09:44
      |
      |2
      |
      |--------------------------------------------------
      |1: Shoma Nishitaten 2018/2/28 (水) 09:44
      |
      |1
      |
      |--------------------------------------------------
      |"
      |"1:2929253","コピー中2","cpyo","Shoma Nishitaten","2018/2/28 09:39","Shoma Nishitaten","2018/2/28 09:39","未着手","C","Shoma Nishitaten","2018/8/19 09:30",""
      |"1:2929251","コピー中1","cpyo","Shoma Nishitaten","2018/2/28 09:39","Shoma Nishitaten","2018/2/28 09:39","未着手","C","","2018/2/28",""
      |"1:2929250","コピー中","cpyo","Shoma Nishitaten","2018/2/28 09:39","Shoma Nishitaten","2018/2/28 09:39","完了","C","","",""
      |"1:2929246","123","aaa","Shoma Nishitaten","2018/2/28 09:38","Shoma Nishitaten","2018/2/28 09:38","保留","C","内田 優一","",""
      |"1:2929244","nbmfkaaa","werfgfdfedfs","Shoma Nishitaten","2018/2/28 09:38","Shoma Nishitaten","2018/2/28 09:38","対応中","S","内田 優一","2018/2/28",""
      |"1:2929242","nbmfk","werfg","Shoma Nishitaten","2018/2/28 09:38","Shoma Nishitaten","2018/2/28 09:38","未着手","S","Shoma Nishitaten","2018/2/28",""
      |"1:2929206","Bをする (コピー)","test","Shoma Nishitaten","2018/2/28 09:15","Shoma Nishitaten","2018/2/28 09:15","未着手","C","","2018/6/28",""
      |"1:2929205","Bをする","test","Shoma Nishitaten","2018/2/28 09:13","Shoma Nishitaten","2018/2/28 09:14","対応中","C","","2018/2/28","--------------------------------------------------
      |1: Shoma Nishitaten 2018/2/28 (水) 09:14
      |
      |comment
      |
      |--------------------------------------------------
      |"
      |"1:2846398","1. Aをする","Assignees 自分","Shoma Nishitaten","2017/12/13 09:57","Shoma Nishitaten","2017/12/13 09:57","保留","A","Shoma Nishitaten","",""
      |""".stripMargin
}