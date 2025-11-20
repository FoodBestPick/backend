package org.example.backend.foodpick.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TagRequest {
    @NotBlank(message = "태그 이름을 입력해주세요.")
    private String name;
}