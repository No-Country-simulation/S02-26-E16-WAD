//package com.elevideo.backend.dto.project;
//
//public record ProjectRequest(String title, String description  ) {
//}

package com.elevideo.backend.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        String description
) {
}
