package vn.luuhien.springrestwithai.feature.user.dto;

import vn.luuhien.springrestwithai.feature.user.User;

public record UserFilterRequest(
        String name,
        String address,
        String email,
        Integer ageFrom,
        Integer ageTo,
        User.GenderEnum gender) {
}
