package vn.team9.auction_system.common.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageMeta {
    private int current;
    private int pageSize;
    private int pages;
    private long total;
}
