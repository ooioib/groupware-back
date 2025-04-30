package org.codenova.groupwareback.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.codenova.groupwareback.entity.Employee;

@Setter
@Getter
@Builder
public class LoginResult {
    private String token;
    private Employee employee;

}
