package com.interviewee.preinter.member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Setter
@RedisHash("member")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    private String username;
    private String displayName;
    private String password;


}
