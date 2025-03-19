package org.zerock.mallapi.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.mallapi.domain.Member;
import org.zerock.mallapi.domain.MemberRole;
import org.zerock.mallapi.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/member/dummies")
@RequiredArgsConstructor
public class DummyController {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @GetMapping("")
    public java.util.List<Member> makeDummies() {


        List<Member> members = new ArrayList<>();


        for (int i = 0; i < 10 ; i++) {

            Member member = Member.builder()
                    .email("user"+i+"@aaa.com")
                    .pw(passwordEncoder.encode("1111"))
                    .nickname("USER"+i)
                    .build();

            member.addRole(MemberRole.USER);

            if(i >= 5){
                member.addRole(MemberRole.MANAGER);
            }

            if(i >=8){
                member.addRole(MemberRole.ADMIN);
            }
            memberRepository.save(member);
            members.add(member);
        }

        return members;
    }
}
