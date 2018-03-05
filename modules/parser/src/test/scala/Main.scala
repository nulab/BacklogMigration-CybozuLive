import com.nulabinc.backlog.c2b.parser.CSVParser

object Main extends App {

  println(CSVParser(source))

  def source =
    """
      |"姓","名","よみがな姓","よみがな名","メールアドレス"
      |"Shoma","Nishitaten","","","shoma.nishitateno@nulab-inc.com"
      |"内田","優一","うちだ","ゆういち","uchida@nulab-inc.com"
    """.stripMargin

}