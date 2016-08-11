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

import java.io.OutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{ResponseHeader, Result, Results}

import scala.concurrent.ExecutionContext
import scala.util.Try

object HttpsStream extends HttpsStream

trait HttpsStream {
  def stream(url: String)(implicit ec: ExecutionContext) = {
    val conn = Try(openConnection(url))
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

  protected def openConnection(url: String) = {
    val conn = new URL(url).openConnection().asInstanceOf[HttpsURLConnection]
    conn.setDoOutput(true)
    conn.setRequestMethod("POST")
    conn
  }
}
