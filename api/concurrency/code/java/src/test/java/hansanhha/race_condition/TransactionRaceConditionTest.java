package hansanhha.race_condition;

import hansanhha.lock.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionRaceConditionTest {

    @Autowired
    private CouponService couponService;

    @Test
    @DisplayName("두 개의 스레드에서 쿠폰을 발급받지만 하나의 잔여 수량만 차감된다")
    void transactionRaceCondition() throws InterruptedException {

        // 초기 쿠폰 수량: 5개
        Long couponId;
        int initialCouponQuantity = 5;
        couponId = couponService.createCoupon("test code", initialCouponQuantity);

        // 두 개의 스레드에서 쿠폰 발급
        Runnable issueCoupon = () -> couponService.issueCoupon(couponId);

        Thread t1 = new Thread(issueCoupon);
        Thread t2 = new Thread(issueCoupon);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // 쿠폰 발급 후 기대 잔여 수량: 3개
        int expectedRemainingQuantity = initialCouponQuantity - 2;

        // 실제 잔여 수량: 4개
        int actualRemainingQuantity = couponService.getRemainingQuantity(couponId);

        // 기대 잔여 수량과 실제 잔여 수량이 다르다
        assertThat(expectedRemainingQuantity).isNotEqualTo(actualRemainingQuantity);

        // 경쟁 조건 발생으로 인해 실제로는 하나만 차감됐다
        assertThat(actualRemainingQuantity).isEqualTo(initialCouponQuantity - 1);
    }

    @Test
    @DisplayName("Serializable 트랜잭션 격리 수준을 사용하면 데드락이 발생한다")
    void serializableIsolationRaiseDeadlock() throws InterruptedException {

        // 초기 쿠폰 수량: 5개
        Long couponId;
        int initialCouponQuantity = 5;
        couponId = couponService.createCoupon("test code", initialCouponQuantity);

        Runnable issueCouponSerializable = () -> couponService.issueCouponSerializable(couponId);

        // Serializable 격리 수준에서 두 개의 스레드에서 쿠폰 발급한다
        // 데드락 발생 (읽기 락을 얻은 상태에서 쓰기락을 얻을 수 없다)
        Thread t1 = new Thread(issueCouponSerializable);
        Thread t2 = new Thread(issueCouponSerializable);

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

}
