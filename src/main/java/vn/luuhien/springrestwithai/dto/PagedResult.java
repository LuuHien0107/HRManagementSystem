package vn.luuhien.springrestwithai.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResult<T>(Meta meta, List<T> result) {

    public static <T> PagedResult<T> from(Page<T> pageData) {
        Meta meta = new Meta(
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalPages(),
                pageData.getTotalElements());
        return new PagedResult<>(meta, pageData.getContent());
    }

    public record Meta(int page, int pageSize, int pages, long total) {
    }
}
