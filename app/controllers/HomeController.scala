package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import play.api.mvc._

import java.util.concurrent.TimeUnit
import javax.inject._

@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  // https://en.wikipedia.org/wiki/Chunked_transfer_encoding
  def index() = Action { implicit request: Request[AnyContent] =>

    // 入力のパラメータを3つに分けるとする
    val input = Seq(0, 1, 2)

    // それぞれのパラメータに応じてデータストアからfetchし、
    // 結果を加工するとこういうCSVができるとする
    val fetchedResult = Seq(
      """a,1
        |b,2
        |c,3
        |d,4
        |f,5
        |g,6
        |i,7
        |j,8
        |k,9
        |""".stripMargin,
      """l,10
        |m,11
        |n,12
        |o,13
        |p,14
        |""".stripMargin,
      """q,15
        |r,16
        |s,17
        |t,18
        |u,19""".stripMargin,
    )

    // これがDBアクセス + 加工の処理だとする
    def proc(i: Int): String = {
      println(s"Processing $i th param")
      TimeUnit.SECONDS.sleep(1) // わかりやすくなるようにsleep
      fetchedResult(i)
    }

    // .next()すると次のCSVブロックが出てくるイテレータ
    val resultIterator = Iterator.from(input).map(proc)

    // イテレータの中身をPlay（というかAkka）の概念に翻訳する
    val content: Source[ByteString, _] =
      Source.fromIterator(() => resultIterator.map(ByteString(_)))

    // レスポンスを返す
    Ok.chunked(
      content,
      inline = true,
      fileName = Some("example.csv")
    )
  }
}
