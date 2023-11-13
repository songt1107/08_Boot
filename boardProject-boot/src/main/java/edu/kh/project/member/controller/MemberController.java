package edu.kh.project.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.kh.project.member.model.dto.Member;
import edu.kh.project.member.model.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RequestMapping("/member")
@Controller
@SessionAttributes({"loginMember"})
public class MemberController {
	
	@Autowired
	private MemberService service;
	

	@PostMapping("/login")
	public String login(Member inputMember, Model model,
						@RequestHeader("referer") String referer,
						RedirectAttributes ra,
						@RequestParam(value="saveId", required = false) String saveId,
						HttpServletResponse resp
						) {
		
		// @RequestHeader(value="referer") String referer
		// -> 요청 HTTP header에서 "referer" (이전 주소) 값을 얻어와
		// 	매개 변수 String referer에 저장
		
		
		// Model : 데이터 전달용 객체
		// -> 데이터를 K : V 형식으로 담아 전달
		// -> 기본적으로 request scope
		// -> @SessionAttributes 어노테이션과 함께 사용 시 session scope
		
		// @RequestParam(value="saveId", required=false) String saveId
		// -> name 속성값이 saveId인 파라미터를 전달받아서 저장
		// required 미작성 시 기본 값 true
		// required = false : 필수 아님(null 허용)
		
		
		
		// 로그인 서비스 호출
		Member loginMember = service.login(inputMember);
		
		// DB 조회 결과 확인
		//System.out.println(loginMember);
		
		// 로그인 결과에 따라 리타이렉트 경로를 다르게 지정
		String path = "redirect:";
		
		if(loginMember != null) { 	// 로그인 성공시
			path += "/";  // 메인페이지로 리다이렉트
			
			// session loginMember 추가
			
			// Session에 로그인한 회원 정보 추가
			// Servlet : HttpSession.setAttribute(key, value)
			// Spring  : Model + @SessionAttributes
			
			// 1) model에 로그인한 회원 정보 추가
			model.addAttribute("loginMember", loginMember);
			// -> 현재는 request scope
			
			// 2) 클래스 위에 @SessionAttributes 추가
			// -> 이제 session scope 
			
			// -------------------------------
			
			// 아이디 저장 (Cookie)
			
			/* Cookie란?
			 * - 클라이언트 측(브라우저)에서 관리하는 파일
			 * 
			 * - 쿠키파일에 등록된 주소 요청 시 마다
			 * 	 자동으로 요청에 첨부되어 서버로 전달됨.
			 * 
			 * - 서버로 전달된 쿠키에
			 *   값 추가, 수정, 삭제 등을 진행한 후
			 *   다시 클라이언트에게 반환
			 * 
			 * */
			
			// 쿠키 생성(해당 쿠키에 담을 데이터를 k:v 로 지정)
			Cookie cookie = new Cookie("saveId", loginMember.getMemberEmail());
			
			if(saveId != null) { // 체크가 되었을 때
				
				// 한 달(30일) 동안 유지되는 쿠키 생성
				cookie.setMaxAge(60*60*24*30); // 초단위 지정
				
				
			} else { // 체크가 안되었을 때 
				
				// 0초 동안 유지되는 쿠키 생성
				// -> 기존에 쿠기가 지정되어있었다면 해당 쿠키를 삭제
				cookie.setMaxAge(0);
				
			}
			
			
			// 클라이언트가 어떤 요청을 할 때 쿠키가 첨부될지 경로(주소)를 지정
			cookie.setPath("/"); // localhost:/ 이하 모든 주소
								// ex) / , /member/login , /member/logout 등
								// 모든 요청에 쿠키를 첨부
			
			// 응답 객체(HttpServletResponse) 를 이용해서
			// 만들어진 쿠키를 클라이언트에게 전달
			resp.addCookie(cookie);
			
			
		} else { // 로그인 실패
			path += referer;
			
			// message 추가 (아이디 또는 비밀번호 불일치)
			
			/* redirect(재요청) 시
			 * 기존 요청(request)이 사라지고
			 * 새로운 요청(request)을 만들게 되어
			 * redirect된 페이지에서는 이전 요청이 유지 되지 않는다!
			 * -> 유지 하고 싶으면 어쩔수 없이 session scope를 이용
			 * 
			 * RedirectAttibutes를 스프링에서 제공
			 * - 리다이렉트 시 데이터를 request scope로 전달할 수 있게하는 객체
			 * 
			 * 응답 전 : request scope
			 * 
			 * 응답 중 : session scope로 잠시 이동
			 * 
			 * 응답 후 : request scope로 복귀
			 * 
			 * */
			
			// addFlashAttribute : 잠시 session 에 추가 
			ra.addFlashAttribute("message", "아이디 또는 비밀번호 불일치");
		}
		
		return path;
	}
	
	@GetMapping("/logout")
	public String logout(SessionStatus status, HttpSession session) {
		status.setComplete();
		return "redirect:/";
	}
	
	// 회원 가입 페이지 이동
		@GetMapping("/signUp")
		public String signUp() {
			
			return "member/signUp";
		}
		
		
	// 회원 가입 진행
	@PostMapping("/signUp")
	public String signUp(Member inputMember,
						String[] memberAddress,
						RedirectAttributes ra ) {	
			
		// Member inputMember : 커맨드 객체 (제출된 파라미터가 저장된 객체)
			
		// String[] memberAddress : 
		//	input name="memberAddress" 3개가 저장된 배열
			
		// RedirectAttributes ra : 
			// 리다이렉트 시 데이터를 request scope로 전달하는 객체
			
			System.out.println("주소 : " + inputMember.getMemberAddress());
			
			// 01234,서울 성동구 어쩌구,2층
			// 만약에 입력하지 않았다면 ,, 이런식으로 구분자만 나옴
			// 주소를 입력하지 않은 경우 null 로 변경 
			if(inputMember.getMemberAddress().equals(",,")) {
				inputMember.setMemberAddress(null);
				
			}else {
				// String.join("구분자", String[])
				// 배열의 요소를 하나의 문자열로 변경
				// 요소 사이에 구분자를 추가함
				String addr = String.join("^^^", memberAddress);
				inputMember.setMemberAddress(addr);
				
			}
			
			
			
			// 회원 가입 서비스 호출
			int result = service.signUp(inputMember);
			
			
			// 가입 성공 여부에 따라서 주소 결정
			String path = "redirect:";
			String message = null;
			
			if(result > 0) { // 가입 성공
				path += "/"; // 메인페이지로
				
				message = inputMember.getMemberNickname() + "님의 가입을 환영합니다";
				
			}else { // 가입 실패
				
				// 회원 가입 페이지
				//path += "/member/signUp"; // 절대경로
				path += "signUp"; // 상대 경로
				
				message = "회원 가입 실패";
				
			}
			
			// 리다이렉트 시 session에 잠깐 올라갔다 request로 복귀하도록 세팅
			ra.addFlashAttribute("message", message);
			
			return path;
		}
		
	
	
}
