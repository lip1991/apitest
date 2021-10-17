package restful.demo.api.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberId")
    private Long id;

    private String stuNo;   // 일반 서비스에서 중복없는 username과 같은 역할

    private String enterYear; // 입학년도(복합키 사용)

    private String name;    // 성명

    private String birthMd; // 생년월일

    private String sustCd;  // 학과코드(법학, 행정학)

    private String mjrCd;   // 전공코드

    private String shysCd;  // 학년코드

    private String shtmCd;  // 학기코드

    private String finSchregDivCd; // 학적상태코드

    private Integer cptnShtmCnt; // 학기수

    private String email; // 이메일

    @Enumerated(EnumType.STRING)
    private Gender gender; // 성별 enum 타입

    @Embedded
    private Address address; // 주소 임베디드 타입


}/////
