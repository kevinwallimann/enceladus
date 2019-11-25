/*
 * Copyright 2018-2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.menas.auth

import java.util.UUID

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import za.co.absa.enceladus.menas.auth.AuthConstants._
import za.co.absa.enceladus.menas.auth.jwt.JwtCookieFactory

@Component
class MenasAuthenticationSuccessHandler @Autowired()(jwtCookieFactory: JwtCookieFactory)
  extends SimpleUrlAuthenticationSuccessHandler {

  override def onAuthenticationSuccess(request: HttpServletRequest,
                                       response: HttpServletResponse,
                                       authentication: Authentication): Unit = {
    val csrfToken = UUID.randomUUID().toString
    response.addHeader(CsrfTokenKey, csrfToken)

    val jwtCookie = jwtCookieFactory.createJwtCookie(authentication, csrfToken, request.getContextPath)
    response.addCookie(jwtCookie)

    clearAuthenticationAttributes(request)
  }

}

