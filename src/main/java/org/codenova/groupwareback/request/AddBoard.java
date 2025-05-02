package org.codenova.groupwareback.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddBoard {

    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
