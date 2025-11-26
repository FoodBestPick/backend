package org.example.backend.foodpick.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.user.model.UserRole;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequest {
    private UserRole role;
}
