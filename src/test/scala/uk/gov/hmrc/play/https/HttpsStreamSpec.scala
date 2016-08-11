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

import java.io.{InputStream, OutputStream}
import javax.net.ssl.HttpsURLConnection

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.{Result, Results}
import play.mvc.Http.Status

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by rob on 09/08/16.
  */
class HttpsStreamSpec extends FlatSpec with Matchers with ScalaFutures with MockitoSugar {
  "HttpsStream#stream" should "return a failed future when the connection fails to start" in {
    val stream = new HttpsStream {
      override def openConnection(url: String) = throw new Exception
    }

    val res = Enumerator("test".getBytes).run(stream.stream(""))

    whenReady(res.failed) {
      _ shouldBe an[Exception]
    }
  }

  it should "return a failed future if the connection cannot write to the output stream" in {
    val stream = new HttpsStream {}

    val res = Enumerator("test".getBytes).run(stream.stream(""))

    whenReady(res.failed) {
      _ shouldBe an[Exception]
    }
  }

  it should "stream to the output when the connection is successful" in {
    val mockConnection = mock[HttpsURLConnection]
    val mockOutputStream = mock[OutputStream]
    when(mockConnection.getOutputStream).thenReturn(mockOutputStream)
    val mockInputStream = mock[InputStream]
    when(mockConnection.getInputStream).thenReturn(mockInputStream)

    val stream = new HttpsStream {
      override def openConnection(url: String) = mockConnection
    }

    val bytes = "test".getBytes

    val res = Enumerator("test".getBytes).run(stream.stream(""))

    whenReady(res) {
      _ => verify(mockOutputStream).write(bytes)
    }
  }

  it should "return a response code when one is received" in {
    val result = Results.Ok("test")

    val mockConnection = mock[HttpsURLConnection]

    val mockOutputStream = mock[OutputStream]

    val mockInputStream = mock[InputStream]

    when(mockConnection.getOutputStream).thenReturn(mockOutputStream)

    when(mockConnection.getResponseCode).thenReturn(Status.OK)

    when(mockConnection.getInputStream).thenReturn(mockInputStream)

    when(mockInputStream.toString).thenReturn("test")

    val stream = new HttpsStream {
      override def openConnection(url: String) = mockConnection
    }

    val res = Enumerator("test".getBytes).run(stream.stream(""))

    whenReady(res) {res =>
      res.header.status shouldBe Status.OK
      whenReady(res.body.run(Iteratee.consume[Array[Byte]]())){
        body => new String(body) shouldBe "test"
      }
    }
  }
  "HttpsStream#openConnection" should "correctly configure the httpConnection" in {
    val stream = new HttpsStream {
      def testReflector(url: String) = openConnection(url)
    }

    val res = stream.testReflector("https://localhost")

    res.getDoOutput shouldBe true
    res.getRequestMethod shouldBe "POST"
  }
}
