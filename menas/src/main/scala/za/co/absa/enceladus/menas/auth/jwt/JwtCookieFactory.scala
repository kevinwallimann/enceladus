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

package za.co.absa.enceladus.menas.auth.jwt

import io.jsonwebtoken.{Jwt, JwtBuilder}
import javax.servlet.http.Cookie
import org.joda.time.{DateTime, DateTimeZone, Hours}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import za.co.absa.enceladus.menas.auth.AuthConstants._

import scala.collection.JavaConverters._

@Component
class JwtCookieFactory @Autowired()(jwtBuilder: JwtBuilder,
                                    @Value("${za.co.absa.enceladus.menas.auth.jwt.lifespan.hours}")
                                    jwtLifespanHours: Int,
                                    @Value("${timezone}")
                                    timezone: String) {

  def createJwtCookie(authentication: Authentication, csrfToken: String, contextPath: String): Cookie = {
    val expiry = Hours.hours(jwtLifespanHours).toStandardSeconds
    val jwtExpirationTime = DateTime.now(DateTimeZone.forID(timezone)).plus(expiry).toDate
    val cookieLifetime = expiry.getSeconds

    val groups = authentication
      .getAuthorities.asScala
      .map(_.getAuthority).asJava

    val jwt = jwtBuilder
      .setSubject(authentication.getName)
      .setExpiration(jwtExpirationTime)
      .claim(CsrfTokenKey, csrfToken)
      .claim(GroupsKey, groups)
      .compact()

    val cookie = new Cookie(JwtCookieKey, jwt)
    cookie.setPath(contextPath)
    cookie.setMaxAge(cookieLifetime)
    cookie
  }

}
