

import com.nulabinc.backlog.c2b.parsers.CSVRecordParser
import org.apache.commons.csv.{CSVFormat, CSVParser}

import scala.collection.JavaConverters._

object IssueTest extends App {

  CSVParser.parse(source, CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()).getRecords.asScala.foreach { r =>
    CSVRecordParser.issue(r) match {
      case Right(issue) =>
        println("======================")
        println(issue)
        println("======================")
      case Left(error) => println("ERROR: " + error.toString)
    }
  }

  def source = """"ID","タイトル","本文","作成者","作成日時","更新者","更新日時","Status","Priority","Assignees","Due Date","コメント"
                 |"1:2929246","メアド違い、同姓同名がコメント","aaa","Shoma Nishitaten","2018/2/28 09:38","Shoma Nishitaten","2018/3/7 11:49","保留","C","内田 優一","","--------------------------------------------------
                 |2: Shoma Nishitaten 2018/3/7 (水) 11:49
                 |
                 |本人
                 |
                 |--------------------------------------------------
                 |1: Shoma Nishitaten 2018/3/7 (水) 11:48
                 |
                 |aaa
                 |
                 |--------------------------------------------------
                 |"
                 |"1:2937458","複数のコメントと複数のコメント行","aa
                 |aaa
                 |aaaa
                 |","Shoma Nishitaten","2018/3/7 10:43","Shoma Nishitaten","2018/3/7 10:44","Not started","B","","2018/3/7","--------------------------------------------------
                 |2: Shoma Nishitaten 2018/3/7 (水) 10:44
                 |
                 |     a
                 |    aa
                 |   aaaa
                 | aaaaaaa
                 |aaaaaaaaa
                 |
                 |--------------------------------------------------
                 |1: Shoma Nishitaten 2018/3/7 (水) 10:44
                 |
                 |123
                 |345
                 |789
                 |
                 |--------------------------------------------------
                 |"""".stripMargin
}

object EventTest extends App {

  try {
    CSVParser.parse(source, CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()).getRecords.asScala.foreach { r =>
      CSVRecordParser.event(r) match {
        case Right(event) =>
          println("======================")
          println(event)
          println("======================")
        case Left(error) => println("ERROR: " + error.toString)
      }
    }
  } catch {
    case ex: Throwable =>
      println(ex.getMessage)
      ex.getStackTrace.foreach(println)
  }

  def source = """"開始日付","開始時刻","終了日付","終了時刻","予定メニュー","タイトル","メモ","作成者","コメント"
                 |"2018/3/1","","2018/3/1","","会議","最初の会議","時間指定なし","Shoma Nishitaten","--------------------------------------------------
                 |2: Shoma Nishitaten 2018/3/1 (木) 15:14
                 |
                 |添付ファイルあり
                 |
                 |--------------------------------------------------
                 |1: Shoma Nishitaten 2018/3/1 (木) 15:14
                 |
                 |a
                 |
                 |--------------------------------------------------
                 |"
                 |"2018/3/2","13:10:00","2018/3/2","13:45:00","往訪","時間指定","","Shoma Nishitaten",""
                 |"2018/3/3","08:00:00","2018/3/3","10:00:00","勉強会","詳細設定","aaaaaaa [file:1]","Shoma Nishitaten",""
                 |"2018/3/3","08:00:00","2018/3/3","10:00:00","勉強会","詳細設定 書式設定なし","aaaaaaa
                 |[file:1]","Shoma Nishitaten",""
                 |""".stripMargin
}

object ForumTest extends App {

  try {
    CSVParser.parse(source, CSVFormat.DEFAULT.withIgnoreEmptyLines().withSkipHeaderRecord()).getRecords.asScala.foreach { r =>
      CSVRecordParser.forum(r) match {
        case Right(data) =>
          println("======================")
          println(data)
          println("======================")
        case Left(error) => println("ERROR: " + error.toString)
      }
    }
  } catch {
    case ex: Throwable =>
      println(ex.getMessage)
      ex.getStackTrace.foreach(println)
  }

  def source = """"ID","タイトル","本文","作成者","作成日時","更新者","更新日時","コメント"
                 |"1:6305695","Wiki-C","aaaaaa","Shoma Nishitaten","2018/3/1 13:48","Shoma Nishitaten","2018/3/8 11:40","--------------------------------------------------
                 |3: Shoma Nishitaten 2018/3/8 (木) 11:40
                 |
                 |添付ファイルあり
                 |
                 |--------------------------------------------------
                 |2: Shoma Nishitaten 2018/3/1 (木) 13:49
                 |
                 |添付
                 |
                 |--------------------------------------------------
                 |1: Shoma Nishitaten 2018/3/1 (木) 13:49
                 |
                 |aaa
                 |
                 |--------------------------------------------------
                 |"
                 |"1:6305702","アンケート","アンケート作成","Shoma Nishitaten","2018/3/1 13:50","Shoma Nishitaten","2018/3/1 13:50",""
                 |"1:6103883","使い方","","Shoma Nishitaten","2017/12/13 10:11","Shoma Nishitaten","2018/2/27 15:57","--------------------------------------------------
                 |4: Shoma Nishitaten 2018/2/27 (火) 15:57
                 |
                 |共有フォルダから
                 |
                 |--------------------------------------------------
                 |3: Shoma Nishitaten 2018/2/27 (火) 15:57
                 |
                 |ファイル添付
                 |
                 |--------------------------------------------------
                 |2: Shoma Nishitaten 2018/2/27 (火) 15:57
                 |
                 |ちがうかも
                 |
                 |--------------------------------------------------
                 |1: Shoma Nishitaten 2018/2/27 (火) 15:57
                 |
                 |forumはwikiなのか
                 |
                 |--------------------------------------------------
                 |"
                 |""".stripMargin
}

//object GenerateCSVRecodeTest extends App {
//
//  val userObservable = Observable(
//    CybozuUser(100, "Nishitateno", "Shoma"),
//    CybozuUser(101, "Aaaa", "Bbb")
//  )
//
//  val record = CSVRecordGenerator.to(userObservable)
//
//  val prg = for {
//    _ <-  StorageDSL.writeFile(File("aaa.csv").path, record)
//  } yield ()
//
//  val interpreter = new LocalStorageInterpreter
//
//  val f = interpreter.run(prg).runAsync
//
//  Await.result(f, Duration.Inf)
//
//}