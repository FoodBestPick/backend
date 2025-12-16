package org.example.backend.foodpick.domain.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.model.InquiryCategory;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCreateRequest {
    private InquiryCategory category;
    private String title;
    private String content;
}
