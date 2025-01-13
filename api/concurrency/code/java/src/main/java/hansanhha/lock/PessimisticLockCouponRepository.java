package hansanhha.lock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PessimisticLockCouponRepository extends CrudRepository<Coupon, Long> {

    // 비관적 읽기 락
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithReadLock(@Param("id") Long id);

    // 비관적 쓰기 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithWriteLock(@Param("id") Long id);

    // 비관적 읽기/쓰기 락 + 강제 버전 증가
    // 연관관계(1:N 등)에 놓인 엔티티 변경 시 부모 엔티티의 버전을 증가시켜 자식 엔티티에 대한 동시성 문제를 방지할 수 있다
    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithWriteVersionLock(@Param("Id") Long id);
}
