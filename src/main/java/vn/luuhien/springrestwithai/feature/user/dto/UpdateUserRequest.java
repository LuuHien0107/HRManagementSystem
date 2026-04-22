package vn.luuhien.springrestwithai.feature.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import vn.luuhien.springrestwithai.feature.user.User;

import java.util.List;

public record UpdateUserRequest(
        @NotNull(message = "Id không được để trống") Long id,

        @NotBlank(message = "Tên không được để trống") @Size(min = 2, max = 100, message = "Tên phải từ 2 đến 100 ký tự") String name,

        @Min(value = 1, message = "Tuổi phải lớn hơn 0") @Max(value = 150, message = "Tuổi không hợp lệ") Integer age,

        @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự") String address,

        User.GenderEnum gender,

        @Size(max = 255, message = "Avatar không được quá 255 ký tự") String avatar,

        Long companyId,

        @NotNull(message = "Danh sách vai trò không được để null") @NotEmpty(message = "Người dùng phải có ít nhất 1 vai trò") List<Long> roleIds) {
}
