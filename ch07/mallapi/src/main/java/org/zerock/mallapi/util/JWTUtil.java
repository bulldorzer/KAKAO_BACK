package org.zerock.mallapi.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;

import java.time.ZonedDateTime;
import java.util.*;

import javax.crypto.SecretKey;

@Log4j2
public class JWTUtil {

  private static String key = "1234567890123456789012345678901234567890";

  public static String generateToken(Map<String, Object> valueMap, int min) {

    SecretKey key = null;

    try {
      key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes("UTF-8"));

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }

    String jwtStr = Jwts.builder().header()
            .add("typ", "JWT")
            .add("alg", "HS256")
            .and()
            .issuedAt(Date.from(ZonedDateTime.now().toInstant()))
            .expiration((Date.from(ZonedDateTime.now()
                    .plusMinutes(min).toInstant()))).claims(valueMap)
            .signWith(key)
            .compact();
    return jwtStr;
  }

  public static Map<String, Object> validateToken(String token) {

    Claims claims = null;
    
    try{

      SecretKey key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes("UTF-8"));

       claims = Jwts.parser().verifyWith(key)
      .build()
      .parseSignedClaims(token)
      .getPayload();
              
    }catch(MalformedJwtException malformedJwtException){
        throw new CustomJWTException("MalFormed");
    }catch(ExpiredJwtException expiredJwtException){
        throw new CustomJWTException("Expired");
    }catch(InvalidClaimException invalidClaimException){
        throw new CustomJWTException("Invalid");
    }catch(JwtException jwtException){
        throw new CustomJWTException("JWTError");
    }catch(Exception e){
        throw new CustomJWTException("Error");
    }
    return claims;
  }

}
