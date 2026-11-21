
package com.project.cravely.dto;

import com.project.cravely.enums.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        UserRole role
) {
}
