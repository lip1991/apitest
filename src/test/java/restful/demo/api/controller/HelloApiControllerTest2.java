package restful.demo.api.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import restful.demo.api.entity.Address;
import restful.demo.api.entity.Gender;
import restful.demo.api.entity.Member;
import restful.demo.api.repository.MemberRepository;
import restful.demo.api.service.MemberService;

/**
 * 스프링 공식 사이트에서 Spring Rest Docs 카테고리 가면 아래 github 샘플 위치 나옴
 * https://github.com/spring-projects/spring-restdocs
 * 거기 있는 코드 그대로 사용해서 성공
 *
 * build를 해보면 알 수 있겠지만 compileJava > test > asciidoctor > bootJar 순으로 실행된다.
 *
 * <성공 이유>
 *   @MockBean 다 없앴음
 *   @AutoConfigureRestDocs 없앴음
 *   @WebMvcTest(HelloApiController.class) 없앴음
 *   responseFields 필드에는 반환 필드들 다 넣어줘야 error 안남
 *
 *   그렇다면, MockBean 사용하려면 어떻게 해야 할까???
 *   하나씩 Mokito 관련 코드들 추가해가면서 오류 잡아가며 진행해야 할듯
 *
 *   MemberService를 통해 값을 예측할 것이기 때문에, Mock 의 대상자는 MemberService 가 된다.
 *
 *   HelloApiControllerTest2는 Mockito를 사용(@ExtendWith)하고, MemberRepository를 실제 객체가 아닌 Mock 객체로 바꾸어 주입(@Mock)합니다.
 *   따라서 테스트 런타임 시 MemberService의 멤버 변수로 선언된 MemberRepository에 Mock 객체가 주입(InjectMocks)됩니다.
 *
 *   @ExtendWith(SpringExtension.class) > @ExtendWith(MockitoExtension.class) 포함하고 있음
 *   
 *   @MockBean 은 @WebMvcTest 또는 @SpringBootTest 일때 사용하는 것
 *    ===>  스프링 컨테이너를 띄우고(@WebMvcTest 또는 @SpringBootTest) 등록되는 Bean 중에서 @MockBean 객체를 Mock 객체로 바꿔줌
 *
 *    Field Injection을 이용해서 Bean을 주입하여 테스트할 때, MockBean, InjectMocks 조합을 사용해 줍니다.
 *
 *    <모키토 프레임워크의 @Mock VS 부트 프레임워크의 @MockBean 차이>
 *      - 두 경우 모두로 구현할 수 있지만, Bean이 Container에 존재해야 할 경우에는 MockBean,
 *        그렇지 않을 경우에는 Mock을 사용한다고 볼 수 있습니다. 저는 아직 경험이 부족하여 두 경우를 구별하는 것이 어렵네요.
 *
 *
 *  mocking을 하여 예상 응답 값 받기(void일 경우 doNothing() 사용)
 *  doNothing().when(userService).save(saveRequest);
 *
 *  슬라이스 테스트 : @SpringBootTest 처럼 모든 컨테이너를 올려 모든 Bean을 등록하여 테스트하는 환경이 아닌
 *                 @WebMvcTest 처럼 일부분만을 짤라 레이어별로 테스트하는 것이 슬라이스 테스트이다
 *
 *  @WebMvcTest : Controller 슬라이스 테스트
 *    - @WebMvcTest 어노테이션을 사용하면 웹 레이어 테스트를 하는 데 필요한 @Controller, @ControllerAdvice, @JsonComponent, Converter, GenericConverter, Filter, WebMvcConfigurer, HandlerMethodArgumentResolver 등만 Bean으로 등록
 *    - 이 밖에 테스트를 하는 데 필요하지 않은 컴포넌트들(ex. @Service, @Repository)은 Bean으로 등록하지 않는다.
 *   @WebFluxTest
 *   @DataJpaTest
 *   @JsonTest
 *   @RestClientTest
 *
 *
 *
 *
 *   *** 이해하고 이제 정리!!!
 *   https://tecoble.techcourse.co.kr/post/2021-05-18-slice-test/
 *
 *   - @SpringBootTest : 스프링 컨테이너를 실행하여 Bean들을 실제 컨테이너에 등록하여 하는 테스트(DB 커넥션해서 데이터 받아오는거 O)
 *   - @WebMvcTest : Controller가 동작하는지만! 테스트 하는 어노테이션(DB 커넥션해서 데이터 받아오는거 X)
 *     - @MockBean : @WebMvcTest 와 같이 사용되며 @Controller 어노테이션만 가짜 객체로 올리므로 의존관계 있는 객체를 @MockBean으로 등록해줘야 함
 *                   Mock Bean은 기존 Bean의 껍데기만 가져오고 내부 구현은 사용자에게 위임한 형태.
 *                   즉, 해당 Bean의 어떤 메서드에 어떤 값이 입력되면 어떤 값이 리턴 되어야 한다는 내용 모두 testExample 메서드와 같이 개발자 필요에 의해서 조작이 가능
 *                   (given, when, then)
 *                   어떤 로직에 대해 Bean이 예상대로 동작하도록 하고 싶을 때, Mock Bean을 사용하는 것이다
 *
 *   - @Mock : 모키토 프레임워크에서 제공하며  Bean이 Container에 존재하지 않아도 될 경우 @Mock 사용
 *   - @InjectMocks : @Mock이 붙은 목객체를 @InjectMocks이 붙은 객체에 주입
 *     - 예) @InjectMocks private MemberService memberService , @Mock private MemberRepository memberRepository;
 *
 *   따라서, 아래 @WebMvcTest 를 이용한 테스트에서는 당연히 DB 반환값이 나오지 않는다. 다만 테스트가 성공했냐 안했냐만 판단하면 됨
 *   DB에서 데이터를 받아와 return 하는것까지 테스트 하려면 결국은 "통합 테스트"를 해야 한다는 것이다
 *   그리고 그건 @SpringBootTest 을 이용하여 스프링 컨테이너를 올린다는 뜻과 같다
 *   그래서 아래처럼 MemberService에 Mock객체(MemberRepository)를 주입하여 사용하는 것은 성공됨
 *   (MockBean 슬라이드 테스트 할 때 사용하는 것 따라서, MockBean을 사용할꺼면 @WebMvcTest + @MockBean 사용하여 "컨트롤러만 통과하는지" 테스트)
 *   (DB 데이터까지 다 리턴받는 "통합 테스트"를 할꺼면 @SpringBootTest 단독으로 사용 또는 @SpringBootTest + @InjectMocks + @Mock 사용)
 */
@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
//@WebMvcTest(HelloApiController.class) // 데이터베이스까지 커넥션 맺어 테스트 하는 환경과는 맞지 않는다
public class HelloApiControllerTest2 {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc; // 요청을 컨트롤러로 전달하는 역할을 한다.

//    @MockBean
//    private MemberService memberService; // /@WebMvcTest와 같이 슬라이스 테스트 할 때 사용

    @InjectMocks
    private MemberService memberService; // 컨트롤러에서 사용하는 서비스가 등록되지 않았기 때문에 @MockBean을 이용하여 의존성 대체

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
//                .apply(springSecurity())    // springSecurity설정이 되어있지 않으면 생략
                .apply(documentationConfiguration(restDocumentation)).build();
    }

    @Test
    public void sample() throws Exception {

        this.mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andDo(print());


//                Member Responses = Member.builder()
//                .id(1L)
//                .stuNo("20218775")
//                .enterYear("2021")
//                .name("InpyoLee")
//                .birthMd("910109")
//                .sustCd("SUSTCD001")
//                .mjrCd("MJRCD002")
//                .shysCd("1")
//                .shtmCd("1")
//                .finSchregDivCd("FinSchregDivCd001")
//                .cptnShtmCnt(1)
//                .email("devopsTest@naver.com")
//                .gender(Gender.MAN)
//                .address(new Address("Jeongjin Villa Room 201", "24-4, Bulgwang-ro 16-gil", "111"))
//                .build();
//
//        given(this.memberService.findMemberOne(1L)) // getVehicleDetails 메서드를 호출하면
//                .willReturn(Responses); // 지정된 객체를 반환하도록 했다.
//
//        ResultActions result = this.mockMvc.perform(
//                get("/")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//        )
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andDo(document("sample",
//                        responseFields(
//                                fieldWithPath("id").description("The Member's email address"),
//                                fieldWithPath("stuNo").description("The Member's address city"),
//                                fieldWithPath("enterYear").description("The Member's address city"),
//                                fieldWithPath("name").description("The Member's address city"),
//                                fieldWithPath("birthMd").description("The Member's address city"),
//                                fieldWithPath("sustCd").description("The Member's address city"),
//                                fieldWithPath("mjrCd").description("The Member's address city"),
//                                fieldWithPath("shysCd").description("The Member's address city"),
//                                fieldWithPath("shtmCd").description("The Member's address city"),
//                                fieldWithPath("finSchregDivCd").description("The Member's address city"),
//                                fieldWithPath("cptnShtmCnt").description("The Member's address city"),
//                                fieldWithPath("email").description("The Member's address city"),
//                                fieldWithPath("gender").description("The Member's address city"),
//                                fieldWithPath("address").description("The Member's address city").optional(),
//                                fieldWithPath("address.city").description("The Member's address city").optional(),
//                                fieldWithPath("address.street").description("The Member's address city").optional(),
//                                fieldWithPath("address.zipcode").description("The Member's address city").optional())
//                ));

//
//        when(memberService.findAll().get(0).getStuNo()).thenReturn("20218775");

//        this.mockMvc.perform(get("/"))
//                .andExpect(status().isOk())
//                .andDo(print())
//                .andDo(document("sample",
//                        responseFields(
//                                fieldWithPath("[].id").description("The Member's email address"),
//                                fieldWithPath("[].stuNo").description("The Member's address city"),
//                                fieldWithPath("[].enterYear").description("The Member's address city"),
//                                fieldWithPath("[].name").description("The Member's address city"),
//                                fieldWithPath("[].birthMd").description("The Member's address city"),
//                                fieldWithPath("[].sustCd").description("The Member's address city"),
//                                fieldWithPath("[].mjrCd").description("The Member's address city"),
//                                fieldWithPath("[].shysCd").description("The Member's address city"),
//                                fieldWithPath("[].shtmCd").description("The Member's address city"),
//                                fieldWithPath("[].finSchregDivCd").description("The Member's address city"),
//                                fieldWithPath("[].cptnShtmCnt").description("The Member's address city"),
//                                fieldWithPath("[].email").description("The Member's address city"),
//                                fieldWithPath("[].gender").description("The Member's address city"),
//                                fieldWithPath("[].address").description("The Member's address city").optional(),
//                                fieldWithPath("[].address.city").description("The Member's address city").optional(),
//                                fieldWithPath("[].address.street").description("The Member's address city").optional(),
//                                fieldWithPath("[].address.zipcode").description("The Member's address city").optional())
//                ));



//        Member member = memberService.findAll().get(0);
//        System.out.println("member = " + member.toString());
//        when(member).thenReturn(postResponses);

    }
}/////