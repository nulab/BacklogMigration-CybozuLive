import com.nulabinc.backlog.c2b.parser.CsvParser


object Main extends App {

  CsvParser.parseUser(source).right.get.foreach(println)

  def source =
    """
      |"姓","名","よみがな姓","よみがな名","メールアドレス"
      |"Shoma","Nishitaten","","","shoma.nishitateno@nulab-inc.com"
      |"LastName","FirstName","LastName","FirstName","test@nulab-inc.com"
    """.stripMargin

}