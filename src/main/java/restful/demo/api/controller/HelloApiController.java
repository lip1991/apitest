package restful.demo.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import restful.demo.api.entity.Member;
import restful.demo.api.repository.MemberRepository;
import restful.demo.api.service.MemberService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HelloApiController {

    private final MemberService memberService;

    @GetMapping("/")
    public List<Member> HelloApi(){
        List<Member> all = memberService.findAll();
        System.out.println("all = " + all.toString());
        return all;
    }

    @GetMapping("/memberOne")
    public Member HelloApiOne(){
        Member member = memberService.findMemberOne(1L);
        return member;
    }


}/////
