package likelion.halo.hamso.service;

import likelion.halo.hamso.domain.Member;
import likelion.halo.hamso.dto.member.MemberDto;
import likelion.halo.hamso.dto.member.MemberJoinDto;
import likelion.halo.hamso.dto.member.MemberLoginDto;
import likelion.halo.hamso.dto.security.TokenInfoDto;
import likelion.halo.hamso.exception.InvalidPasswordException;
import likelion.halo.hamso.exception.MemberDuplicateException;
import likelion.halo.hamso.exception.NotFoundException;
import likelion.halo.hamso.repository.MemberRepository;
import likelion.halo.hamso.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
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
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String join(MemberJoinDto memberInfo){
        // loginID 중복 확인
        memberRepository.findByLoginId(memberInfo.getLoginId())
                .ifPresent(u -> {
                    throw new MemberDuplicateException("ID '" + memberInfo.getLoginId()+"' is already existed.");
                });

        Member member = Member.builder()
                .loginId(memberInfo.getLoginId())
                .password(encoder.encode(memberInfo.getPassword()))
                .email(memberInfo.getEmail())
                .name(memberInfo.getName())
                .phoneNo(memberInfo.getPhoneNo())
                .address(memberInfo.getAddress())
                .specificAddress(memberInfo.getSpecificAddress())
                .build();

        memberRepository.save(member);
        return member.getLoginId();
    }

    public Boolean checkDuplicate(String loginId){
        // loginID 중복 확인
        memberRepository.findByLoginId(loginId)
                .ifPresent(u -> {
                    throw new MemberDuplicateException("ID '" + loginId+"' is already existed.");
                });

        return true;
    }



    private static List<MemberDto> convertMemberToMemberDto(List<Member> memberList) {
        List<MemberDto> memberDtoList = memberList.stream()
                .map(a -> new MemberDto(a))
                .collect(Collectors.toList());
        return memberDtoList;
    }



    @Transactional
    public TokenInfoDto login(MemberLoginDto memberInfo) {
        // userName X
        Member selectedMember = memberRepository.findByLoginId(memberInfo.getLoginId())
                .orElseThrow(() -> new NotFoundException("Member not found with loginId: " + memberInfo.getLoginId())); // 404
        // password wrong
        if(!encoder.matches(memberInfo.getPassword(), selectedMember.getPassword())) {
            throw new InvalidPasswordException("This is wrong password."); // 401
        }
        log.info("memberInfo = {}", memberInfo);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberInfo.getLoginId(), memberInfo.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        log.info("TokenInfo  authentication = {}", authentication);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenInfoDto tokenInfo = jwtTokenProvider.generateToken(authentication);

        return tokenInfo;
    }

    @Transactional
    public Boolean findAndUpdatePassword(String newPassword, String phoneNo) {
        Optional<Member> oMember = memberRepository.findByPhoneNo(phoneNo);
        if(oMember.isEmpty()) {
            throw new NotFoundException("존재하지 않는 회원의 핸드폰 번호 정보입니다.");
        }
        Member member = oMember.get();
        member.setPassword(encoder.encode(newPassword));
        return true;
    }

    public String findLoginId(String name, String phoneNo) {
        Optional<Member> oMember = memberRepository.findByNameAndPhoneNo(name, phoneNo);
        if(oMember.isEmpty()) {
            throw new NotFoundException("존재하지 않는 회원 정보입니다.");
        }
        return oMember.get().getLoginId();
    }
}
