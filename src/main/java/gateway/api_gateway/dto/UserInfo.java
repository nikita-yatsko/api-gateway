package gateway.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfo {

    private boolean valid;
    private Long userId;
    private String role;
    private String username;
}