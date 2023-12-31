package likelion.halo.hamso.dto.member;

import likelion.halo.hamso.domain.Member;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter @ToString
public class MemberJoinDto {
    private String loginId; // 로그인 아이디

    private String password;

    private String name; // 회원의 이름

    private String phoneNo; // 전화번호

    private String email; // 이메일

    private String address; // 주소

    private String specificAddress; // 상세주소

    public MemberJoinDto(Member member) {
        this.loginId = member.getLoginId();
        this.name = member.getName();
        this.phoneNo = member.getPhoneNo();
        this.email = member.getEmail();
        this.address = member.getAddress();
        this.specificAddress = member.getSpecificAddress();
    }

}
