package com.interviewee.preinter.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    String register(Authentication auth) {
        if(auth.isAuthenticated()){
            return "redirect:/list";
        }
        return "register.html";
    }

    @PostMapping("/member")
    String addMember(String username, String password, String displayName) {
        Member member = new Member();

        member.setUsername(username);
        String hash = passwordEncoder.encode(password);
        member.setPassword(hash);
        member.setDisplayName(displayName);

        this.memberRepository.save(member);
        return "redirect:/list";
    }

    @GetMapping("login")
    public String login(){
        return "login.html";
    }

    @GetMapping("/my-page")
    public String myPage(Authentication auth){
        Object result = (CustomUser) auth.getPrincipal();

        if(auth.isAuthenticated()){

        }
        return "mypage.html";
    }
}
class MemberDto{
    public String username;
    public String displayName;
    MemberDto(String a, String b){
        this.username = a;
        this.displayName = b;

    }
}