package vn.team9.auction_system.common.base;

/**
 * Interface base để các service cụ thể kế thừa.
 */
public interface BaseService<TReq, TRes> {
    TRes create(TReq request);
    TRes update(Long id, TReq request);
    void delete(Long id);
    TRes findById(Long id);
}
