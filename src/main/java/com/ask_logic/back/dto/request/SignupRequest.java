package com.ask_logic.back.dto.request;

import com.ask_logic.back.domain.User.Level;
import lombok.Getter;

@Getter
public class SignupRequest {
    private String email;
    private String password;
    private String nickname;
    private Level level; // ELEMENTARY, UNIV, GRAD
}
