package vn.team9.auction_system.common.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private PageMeta meta;
    private List<T> result;
}
