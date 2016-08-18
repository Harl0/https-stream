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

import java.net.{HttpURLConnection, URL}
import javax.net.ssl.{HttpsURLConnection, SSLContext}

/**
  * Created by jason on 16/08/16.
  */

object ConnectionProvider extends ConnectionProvider

trait ConnectionProvider {

  def getConnection(url: String): HttpURLConnection = {

    val protocolURL = new URL(url)
    val protocol = protocolURL.getProtocol()
    val conn = protocol match {
      case "https" => getSecureConnection(protocolURL)
      case "http" => new URL(url).openConnection().asInstanceOf[HttpURLConnection]
      case _ => throw new UnsupportedOperationException("You have supplied an invalid URL," + protocol)
    }

    conn
  }

  protected def getSecureConnection(url: URL) = {

    val connection: HttpsURLConnection = url.openConnection().asInstanceOf[HttpsURLConnection]
    val sslContext = SSLContext.getDefault
    connection.setSSLSocketFactory(sslContext.getSocketFactory())
    connection
  }

}

