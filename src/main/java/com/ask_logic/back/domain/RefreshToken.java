package com.ask_logic.back.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String email;

    @Column(nullable = false, length = 512)
    private String token;

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
