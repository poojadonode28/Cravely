
package com.project.cravely.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDeliveryPartnerRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[+]?[0-9]{10,15}$",
                message = "Invalid phone number"
        )
        String phone
) {
}
