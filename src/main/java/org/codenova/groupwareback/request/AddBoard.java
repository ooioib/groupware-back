package org.codenova.groupwareback.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddBoard {

    // 제목이 null이거나 공백이면 검증 실패
    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
