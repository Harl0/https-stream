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

package uk.gov.hmrc.play.https.helpers

import java.net.URL
import java.security.KeyStore
import javax.net.ssl.{HttpsURLConnection, SSLContext, TrustManagerFactory}

import uk.gov.hmrc.play.https.{HttpsStream, SecureConnectionProvider}

/**
  * Created by jason on 17/08/16.
  */
class SecureConnectionProviderImpl extends HttpsStream {

  val secureConnectionProvider: SecureConnectionProvider = new KeyStoreImpl()

}

class KeyStoreImpl extends SecureConnectionProvider {

  def getTLSConnection(url: String): HttpsURLConnection = {

    // vanilla HTTP connection
    val theURL = new URL(url)
    val connection: HttpsURLConnection = theURL.openConnection().asInstanceOf[HttpsURLConnection]

    // convert to HTTPS via SSL context
    // @TODO paramtarise the getInstance
    val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")

    // initalise the trustStore using the KeyStore which contains the certs
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(KeyStore.getInstance(KeyStore.getDefaultType))
    val trustManagers = tmf.getTrustManagers()

    // use our loaded trust/ keystore
    sslContext.init(null, trustManagers, null)

    // uses our sslContext to apply to the SSL connection
    connection.setSSLSocketFactory(sslContext.getSocketFactory())

    // Returns connection
    connection
  }
}
