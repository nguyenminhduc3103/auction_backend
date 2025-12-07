package vn.team9.auction_system.common.dto.product;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.team9.auction_system.common.base.BaseRequest;

import java.math.BigDecimal;

/**
 * DTO for admin to approve product and set pricing
 * Only admin can use this request to set deposit and estimatePrice
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductApprovalRequest extends BaseRequest {
    private BigDecimal deposit;
    private BigDecimal estimatePrice;
    private String status; // typically "approved" or "rejected"
}
