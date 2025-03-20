package org.zerock.mallapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.zerock.mallapi.domain.Member;
import org.zerock.mallapi.domain.MemberRole;
import org.zerock.mallapi.dto.MemberDTO;
import org.zerock.mallapi.dto.MemberModifyDTO;
import org.zerock.mallapi.repository.MemberRepository;

import java.util.LinkedHashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public MemberDTO getKakaoMember(String accessToken) {

        String email = getEmailFromKakaoAccessToken(accessToken); // 이메일 추출함 db에서 조회
        log.info("email: "+ email);

        Optional<Member> result = memberRepository.findById(email); // DB조회

        // 값이 있으면 기존의 회원 isPresent()<->empty()
        if (result.isPresent()){
            MemberDTO memberDTO = entityToDTO(result.get());
            return memberDTO;
        }
        // 회원 아닐시 닉네임은 소셜회원으로 패스워드는 임의로 생성
        Member socialMember = makeSocialMember(email); // 신규 회원 생성
        memberRepository.save(socialMember); // 엔티티 db에 저장
        MemberDTO memberDTO = entityToDTO(socialMember); //dto 변환후 반환

        return memberDTO;
    }

    @Override
    public void modifyMember(MemberModifyDTO memberModifyDTO) {

        Optional<Member> result = memberRepository.findById(memberModifyDTO.getEmail());

        Member member = result.orElseThrow();

        member.changePw(passwordEncoder.encode(member.getPw()));
        member.changeNickname(memberModifyDTO.getNickname());
        member.changeSocial(false);

        memberRepository.save(member);

    }

    /*
        인가코드를 송신하여 카카오API서버에서 수신받은 AccessToken을 해당 웹서버API에 전달받아
        카카오API서버로 AccessToken을 송신하여 사용자 정보를 수신받는 기능
    */
    private String getEmailFromKakaoAccessToken(String accessToken){
        String kakaoGetUserURL = "https://kapi.kakao.com/v2/user/me";
        
        if (accessToken == null){
            throw new RuntimeException("Access Token is null");
        }

        RestTemplate restTemplate = new RestTemplate(); // http 요청을 보내기 위한 객체

        HttpHeaders headers = new HttpHeaders(); // 헤더설정
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        
        // http 요청 or 응답을 나타내는 객체 생성 - hearder만 설정하고 body 없는 상태임
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // URL을 동적으로 생성 - 이것을 기반으로 URI 객체 생성함
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(kakaoGetUserURL).build();

        // RestTemplate로 get 요청을 보냄
        // JSON응답을 -> LinkedHashMap 객체로 받음
        ResponseEntity<LinkedHashMap> response = restTemplate.exchange(
                uriBuilder.toString(), // 요청 url
                HttpMethod.GET, // 요청메서드
                entity,// 요청 헤더 정보
                LinkedHashMap.class); // 응답(JSON)을 LinkedHashMap 타입으로 변환
        log.info("---------------------------------------");
        log.info(response);

        LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();

        log.info("---------------<bodyMap>------------------------");
        log.info(bodyMap);
        LinkedHashMap<String, String> kakaoAccount = bodyMap.get("kakao_account");

        return kakaoAccount.get("email");

    }

    // 난수 비밀번호 생성
    private String makeTempPassword(){
        StringBuffer buffer = new StringBuffer();

        for (int i=0; i<10; i++){
            buffer.append( (char) ( (int)(Math.random()*55)+65));
        }// 랜덤한 10자리 숫자 임시비밀번호 생성 0~54까지의 난수 65~119까지 생성
        // 아스키 코드에서 대문자 A가 65
        return buffer.toString();
    }

    // 소셜로 로그인한 정보에 아무도 모르는 난수 비밀번호 삽입후 DB에 저장 (회원등록) - USER권한
    private Member makeSocialMember(String email){

        String tempPassword = makeTempPassword();
        log.info("tempPassword : "+tempPassword);

        String nickname = "소셜회원";

        Member member = Member.builder()
                .email(email)
                .pw(passwordEncoder.encode(tempPassword))
                .nickname(nickname)
                .social(true) // 카카오로 연동 되었다,아니다
                .build();

        member.addRole(MemberRole.USER);
        return member;
    }
    
}
