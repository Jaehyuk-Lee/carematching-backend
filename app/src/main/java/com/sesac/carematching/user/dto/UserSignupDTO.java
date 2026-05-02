package com.sesac.carematching.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "회원 가입 DTO")
@Data
public class UserSignupDTO {
    @Schema(description = "아이디")
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(max = 50, message = "아이디는 최대 50자까지 가능합니다.")
    private String username;

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(max = 255, message = "비밀번호는 최대 255자까지 가능합니다.")
    private String password;

    @Schema(description = "비밀번호 확인")
    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    @Size(max = 255, message = "비밀번호는 최대 255자까지 가능합니다.")
    private String confirmPassword;

    @Schema(description = "닉네임")
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    private String nickname;
}
