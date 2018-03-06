import com.nulabinc.backlog.c2b.parser.CsvParser


object Main extends App {

  source.split("\n").drop(1)
    .filterNot(_.isEmpty)
    .map { line =>
      CsvParser.user(line)
    }.foreach(println)

  def source =
    """""姓","名","よみがな姓","よみがな名","メールアドレス"
      |"Shoma","Nishitaten","","","shoma.nishitateno@nulab-inc.com"
      |"内田","優一","うちだ","ゆういち","uchida@nulab-inc.com"
      |""".stripMargin

}