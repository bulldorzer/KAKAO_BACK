package org.zerock.mallapi.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.mallapi.dto.MemberDTO;
import org.zerock.mallapi.service.MemberService;
import org.zerock.mallapi.util.JWTUtil;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class SocialController {

    private final MemberService memberService;

    @GetMapping("/api/member/kakao")
    public Map<String,Object> getMemberFromKakao(String accessToken){

        MemberDTO memberDTO = memberService.getKakaoMember(accessToken);

        Map<String,Object> claims = memberDTO.getClaims(); // 맵형식으로 변환

        // 현재 내서버에서 accessToken, refreshToken 발급받아서 추가
        String jwtAccessToken = JWTUtil.generateToken(claims,10);
        String jwtRefreshToken = JWTUtil.generateToken(claims, 60*24);

        claims.put("accessToken", jwtAccessToken);
        claims.put("refreshToken", jwtRefreshToken);

        return claims;
    }
}
