/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.https

import java.net.HttpURLConnection

import play.api.libs.iteratee.Iteratee
import play.api.mvc.Results

import scala.concurrent.ExecutionContext
import scala.util.Try

object HttpsStream extends HttpsStream

trait HttpsStream {
  val connectionProvider:ConnectionProvider = ConnectionProvider //Expecting a secure socket provider
  def stream(url: String, extraHeaders: Map[String, String] = Map.empty)(implicit ec: ExecutionContext) = {
    val conn = Try(openConnection(url, extraHeaders))
    doStreaming(conn)
  }

  private def doStreaming(conn:Try[HttpURLConnection])(implicit ec: ExecutionContext) = {
    val out = conn.map(_.getOutputStream)

    Iteratee.foreach[Array[Byte]](bytes => out.get.write(bytes))
      .map{_ =>
        out.foreach(_.close())
        conn.map { conn =>
          val code = conn.getResponseCode
          val body = conn.getInputStream.toString
          conn.disconnect()
          Results.Status(code)(body)
        }.get
      }
  }

  protected def openConnection(url: String, extraHeaders: Map[String, String] = Map.empty) = {

    val conn = connectionProvider.getConnection(url)
    conn.setDoOutput(true)
    conn.setRequestMethod("POST")
    extraHeaders.foreach{ case (key, value) => conn.setRequestProperty(key, value) }
    conn
  }
}
