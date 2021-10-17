package restful.demo.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import restful.demo.api.entity.Member;
import restful.demo.api.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Member findMemberOne(long id) {
        return memberRepository.findByMemberOne(id);
    }
}
