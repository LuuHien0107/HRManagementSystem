package vn.luuhien.springrestwithai.feature.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank(message = "Tên công ty không được để trống") @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự") String name,

        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự") String description,

        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự") String address,

        @Size(max = 255, message = "Logo không được vượt quá 255 ký tự") String logo) {
}
