package com.sesac.carematching.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "상태 DTO")
@Data
public class StatusDTO {
    @Schema(description = "상태", allowableValues = {"true", "false"})
    private boolean status;
}
