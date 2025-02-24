package com.sesac.carematching.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    @Size(max = 255, message = "비밀번호는 최대 255자까지 가능합니다.")
    private String currentPassword;

    @Size(max = 255, message = "새 비밀번호는 최대 255자까지 가능합니다.")
    private String newPassword;

    @Size(max = 255, message = "비밀번호 확인은 최대 255자까지 가능합니다.")
    private String confirmPassword;

    @Size(max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    private String nickname;

    @Size(max = 12, message = "전화번호는 최대 12자까지 가능합니다.")
    private String phoneNumber;
}
