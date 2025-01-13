package hansanhha.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.LinkedList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PessimisticLockTest {

    @Autowired
    private CouponService couponService;

    private Long couponId;
    private int couponQuantity = 100000;

    @BeforeEach
    void createCoupon() {
        couponId = couponService.createCoupon(UUID.randomUUID().toString(), couponQuantity);
    }

    @Test
    @DisplayName("비관적 읽기 락을 사용한 쿠폰 발급")
    void pessimisticReadLockIssueCoupon() throws InterruptedException {
        int threadCount = 1000;
        int initialQuantity = couponService.getRemainingQuantity(couponId);
        Runnable task = () -> couponService.issueCouponWithPessimisticReadLock(couponId);

        LinkedList<Thread> threads = new LinkedList<>();
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(task));
        }

        StopWatch stopWatch = new StopWatch("스레드 " + threadCount + "개 비관적 읽기 락 테스트");
        stopWatch.start("전체 실행 시간");

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        int updateQuantity = couponService.getRemainingQuantity(couponId);
        assertThat(updateQuantity).isEqualTo(initialQuantity - threadCount);
        System.out.println("쿠폰 초기 수량:" + initialQuantity);
        System.out.println("발급 후 수량:" + updateQuantity);
    }

    @Test
    @DisplayName("비관적 쓰기 락을 사용한 쿠폰 발급")
    void pessimisticWriteLockIssueCoupon() throws InterruptedException {
        int threadCount = 1000;
        int initialQuantity = couponService.getRemainingQuantity(couponId);
        Runnable task = () -> couponService.issueCouponWithPessimisticWriteLock(couponId);

        LinkedList<Thread> threads = new LinkedList<>();
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(task));
        }

        StopWatch stopWatch = new StopWatch("스레드 " + threadCount + "개 비관적 쓰기 락 테스트");
        stopWatch.start("전체 실행 시간");

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        int updateQuantity = couponService.getRemainingQuantity(couponId);
        assertThat(updateQuantity).isEqualTo(initialQuantity - threadCount);
        System.out.println("쿠폰 초기 수량:" + initialQuantity);
        System.out.println("발급 후 잔여 수량:" + updateQuantity);
    }

//    @Test
    @DisplayName("스레드 10만개에서 비관적 쓰기 락을 사용한 쿠폰 발급")
    void pessimisticWriteLockIssueCouponWith100000Thread() throws InterruptedException {
        int threadCount = 100000;
        int initialQuantity = couponService.getRemainingQuantity(couponId);
        Runnable task = () -> couponService.issueCouponWithPessimisticWriteLock(couponId);

        LinkedList<Thread> threads = new LinkedList<>();
        for (int i = 0; i < threadCount; i++) {
            threads.add(new Thread(task));
        }

        StopWatch stopWatch = new StopWatch("스레드 " + threadCount + "개 비관적 쓰기 락 테스트");
        stopWatch.start("전체 실행 시간");

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

        int updateQuantity = couponService.getRemainingQuantity(couponId);
        assertThat(updateQuantity).isEqualTo(initialQuantity - threadCount);
        System.out.println("쿠폰 초기 수량:" + initialQuantity);
        System.out.println("발급 후 잔여 수량:" + updateQuantity);
    }
}
