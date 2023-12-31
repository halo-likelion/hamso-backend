package likelion.halo.hamso.service;

import likelion.halo.hamso.domain.Member;
import likelion.halo.hamso.dto.member.MemberDto;
import likelion.halo.hamso.dto.member.MemberLoginDto;
import likelion.halo.hamso.dto.member.MemberUpdateAllDto;
import likelion.halo.hamso.exception.NotFoundException;
import likelion.halo.hamso.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    public MemberDto findById(Long id){
        Member member = memberRepository.findById(id).get();
        MemberDto memberDto = new MemberDto(member);
        return memberDto;
    }

    public MemberDto findByLoginId(String loginId){
        Optional<Member> member = memberRepository.findByLoginId(loginId);
        if(member.isEmpty()) {
            // Member not found, handle accordingly
            throw new NotFoundException("Member not found with loginId: " + loginId);
        } else {
            return new MemberDto(member.get());
        }
    }

    public Member findByLoginIdForReal(String loginId){
        Optional<Member> member = memberRepository.findByLoginId(loginId);
        if(member.isEmpty()) {
            // Member not found, handle accordingly
            throw new NotFoundException("Member not found with loginId: " + loginId);
        } else {
            return member.get();
        }
    }

    public List<MemberDto> findAll() {
        List<Member> all = memberRepository.findAll();
        List<MemberDto> memberDtoList = convertMemberToMemberDto(all);
        return memberDtoList;
    }

    @Transactional
    public void updateMemberAll(MemberUpdateAllDto memberInfo) {
        Optional<Member> oMember = memberRepository.findByLoginId(memberInfo.getLoginId());
        if(oMember.isEmpty()) {
            // Member not found, handle accordingly
            throw new NotFoundException("Member not found with loginId: " + memberInfo.getLoginId());
        } else {
            Member member = oMember.get();
            member.setEmail(memberInfo.getEmail());
            member.setPhoneNo(memberInfo.getPhoneNo());
            member.setAddress(memberInfo.getAddress());
            member.setSpecificAddress(memberInfo.getSpecificAddress());
        }

    }

    @Transactional
    public void deleteMember(String loginId) {
        Optional<Member> oMember = memberRepository.findByLoginId(loginId);
        if(oMember.isEmpty()) {
            // Member not found, handle accordingly
            throw new NotFoundException("Member not found with loginId: " + loginId);
        } else {
            Member member = oMember.get();
            memberRepository.delete(member);
        }
    }

//    @Transactional
//    public void updatePassword(MemberLoginDto memberInfo) {
//        Optional<Member> oMember = memberRepository.findByLoginId(memberInfo.getLoginId());
//        if(oMember.isEmpty()) {
//            throw new NotFoundException("Member not found with loginId: " + memberInfo.getLoginId());
//        } else {
//            oMember.get().setPassword(memberInfo.getPassword());
//        }
//    }

    private static List<MemberDto> convertMemberToMemberDto(List<Member> memberList) {
        List<MemberDto> memberDtoList = memberList.stream()
                .map(a -> new MemberDto(a))
                .collect(Collectors.toList());
        return memberDtoList;
    }

    public boolean checkPhoneNoDuplicate(String phoneNo) {
        int cnt = memberRepository.getCountPhoneNo(phoneNo);
        if(cnt>0) {
            return true;
        }
        return false;
    }

    @Transactional
    public Boolean updatePassword(String loginId, String oldPassword, String newPassword) {
        Optional<Member> oMember = memberRepository.findByLoginId(loginId);
        if(oMember.isEmpty()) {
            throw new NotFoundException("Member not found with loginId: " + loginId);
        }
        Member member = oMember.get();

        if(encoder.matches(oldPassword, member.getPassword())) {
            member.setPassword(encoder.encode(newPassword));
        } else {
            throw new NotFoundException("비밀 번호가 일치하지 않습니다." );
        }
        return true;
    }
}
