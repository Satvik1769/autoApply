package com.autoapply.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JdExtractRequest {

    @NotBlank(message = "Job description text is required")
    @Size(min = 50, max = 20000, message = "JD text must be between 50 and 20000 characters")
    private String jdText;
}
