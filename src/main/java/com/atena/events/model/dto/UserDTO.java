package com.atena.events.model.dto;

import com.atena.events.model.AccountType;
import com.atena.events.model.User;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String avatarBase64;
    private int eventsCreatedCount;
    private int participationsCount;
    private boolean guest;
    private String username;
    private String avatarUrl;
    // true quando a conta tem senha local (permite trocar senha / email);
    // false para contas OAuth (Google/GitHub) e convidados.
    private boolean hasPassword;

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.avatarBase64 = user.getAvatarBase64();
        this.avatarUrl = user.getAvatarUrl();
        this.eventsCreatedCount = 0;
        this.participationsCount = 0;
        this.guest = user.getAccountType() == AccountType.GUEST;
        this.username = user.getHandle();
        this.hasPassword = user.getPassword() != null;
    }

    public UserDTO(User user, int eventsCreatedCount, int participationsCount) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.avatarBase64 = user.getAvatarBase64();
        this.avatarUrl = user.getAvatarUrl();
        this.eventsCreatedCount = eventsCreatedCount;
        this.participationsCount = participationsCount;
        this.guest = user.getAccountType() == AccountType.GUEST;
        this.username = user.getHandle();
        this.hasPassword = user.getPassword() != null;
    }
}
