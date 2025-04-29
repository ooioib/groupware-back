package org.codenova.groupwareback.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Login {
    @NotBlank
    private String id;

    @NotBlank
    private String password;
}
