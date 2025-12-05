package org.example.backend.foodpick.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MatchingResponse {

    private boolean matched;

    private Long roomId;
}
