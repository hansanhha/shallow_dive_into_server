package hansanhha.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    private final PessimisticLockCouponRepository pessimisticLockCouponRepository;
    private final OptimisticLockCouponRepository optimisticLockCouponRepository;

    public int getRemainingQuantity(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        return coupon.getRemainingQuantity();
    }

    public int getOptimisticRemainingQuantity(Long couponId) {
        OptimisticCoupon optimisticCoupon = optimisticLockCouponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        return optimisticCoupon.getRemainingQuantity();
    }

    public Long createCoupon(String code, int quantity) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setRemainingQuantity(quantity);

        Coupon save = couponRepository.save(coupon);
        return save.getId();
    }

    public Long createOptimisticCoupon(String code, int quantity) {
        OptimisticCoupon optimisticCoupon = new OptimisticCoupon();
        optimisticCoupon.setCode(code);
        optimisticCoupon.setRemainingQuantity(quantity);

        OptimisticCoupon save = optimisticLockCouponRepository.save(optimisticCoupon);
        return save.getId();
    }

    public void issueCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        coupon.decreaseRemainingQuantity();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void issueCouponSerializable(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        coupon.decreaseRemainingQuantity();
    }

    public void issueCouponWithPessimisticReadLock(Long couponId) {
        Coupon coupon = pessimisticLockCouponRepository.findByIdWithReadLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        coupon.decreaseRemainingQuantity();
    }

    public void issueCouponWithPessimisticWriteLock(Long couponId) {
        Coupon coupon = pessimisticLockCouponRepository.findByIdWithWriteLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        coupon.decreaseRemainingQuantity();
    }

    public void issueCouponWithOptimisticReadLock(Long couponId) {
        OptimisticCoupon optimisticCoupon = optimisticLockCouponRepository.findByIdWithReadLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        optimisticCoupon.decreaseRemainingQuantity();
    }

    public void issueCouponWithOptimisticWriteVersionLock(Long couponId) {
        OptimisticCoupon optimisticCoupon = optimisticLockCouponRepository.findByIdWithWriteVersionLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

        optimisticCoupon.decreaseRemainingQuantity();
    }

}
