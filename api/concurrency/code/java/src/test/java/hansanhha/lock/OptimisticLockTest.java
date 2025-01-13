package hansanhha.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OptimisticLockTest {

    @Autowired
    private CouponService couponService;

    private Long couponId;
    private int couponQuantity = 100000;

    @BeforeEach
    void createCoupon() {
        couponId = couponService.createOptimisticCoupon(UUID.randomUUID().toString(), couponQuantity);
    }

    @Test
    @DisplayName("하나의 스레드에서 낙관적 락을 통해 쿠폰 발급")
    void optimisticLockIssueCouponSuccessful() throws InterruptedException {
        int initialQuantity = couponService.getOptimisticRemainingQuantity(couponId);

        Thread thread = new Thread(() -> couponService.issueCouponWithOptimisticReadLock(couponId));

        thread.start();
        thread.join();

        int updateQuantity = couponService.getOptimisticRemainingQuantity(couponId);

        assertThat(updateQuantity).isEqualTo(initialQuantity - 1);
    }

    @Test
    @DisplayName("여러 스레드에서 낙관적 락을 가지고 쿠폰 발급 시 버전 충돌 발생")
    void optimisticLockIssueCouponVersionConflict() throws InterruptedException {
        int initialQuantity = couponService.getOptimisticRemainingQuantity(couponId);
        int threadCount = 10000;
        List<ObjectOptimisticLockingFailureException> exceptions = Collections.synchronizedList(new LinkedList<>());

        Runnable issueCouponWithOptimisticLock = () -> {
            try {
                couponService.issueCouponWithOptimisticReadLock(couponId);
            } catch (ObjectOptimisticLockingFailureException e) {
                exceptions.add(e);
            }
        };

        LinkedList<Thread> threads = new LinkedList<>();
        threads.addFirst(new Thread(() -> couponService.issueCouponWithOptimisticReadLock(couponId)));
        for (int i = 1; i < threadCount; i++) {
            threads.add(new Thread(issueCouponWithOptimisticLock));
        }

        StopWatch stopWatch = new StopWatch("낙관적 락 버전 충돌 시 롤백");
        stopWatch.start();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();

        int updateQuantity = couponService.getOptimisticRemainingQuantity(couponId);

        System.out.println("쿠폰 초기 수량: " + initialQuantity);
        System.out.println("발급된 수량: " + (initialQuantity - updateQuantity));
        System.out.println("발급하지 못한 수량: " + (threadCount - (initialQuantity - updateQuantity)));
        System.out.println("스레드 개수: " + threadCount );
        System.out.println("버전 충돌 횟수: " + exceptions.size());
        System.out.println(stopWatch.prettyPrint());
        assertThat(exceptions).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("낙관적 락 버전 충돌된 트랜잭션 재시도")
    void optimisticLockIssueCouponVersionConflictRetry() throws InterruptedException {
        int initialQuantity = couponService.getOptimisticRemainingQuantity(couponId);
        int threadCount = 10000;
        int retryMaxAttempt = 5;
        List<ObjectOptimisticLockingFailureException> exceptions = Collections.synchronizedList(new LinkedList<>());
        List<Integer> retryTotalCounts = Collections.synchronizedList(new ArrayList<>());

        Runnable issueCouponWithOptimisticLockRetryable = () -> {
            int retryCount = 0;
            boolean success = false;

            while (!success) {
                try {
                    couponService.issueCouponWithOptimisticReadLock(couponId);
                    success = true;
                } catch (ObjectOptimisticLockingFailureException e) {
                    exceptions.add(e);
                    retryCount++;
                    if (retryCount > retryMaxAttempt) {
                        break;
                    }
                }
            }
            retryTotalCounts.add(retryCount);
        };


        LinkedList<Thread> threads = new LinkedList<>();
        threads.addFirst(new Thread(() -> couponService.issueCouponWithOptimisticReadLock(couponId)));
        for (int i = 1; i < threadCount; i++) {
            threads.add(new Thread(issueCouponWithOptimisticLockRetryable));
        }

        StopWatch stopWatch = new StopWatch("낙관적 락 버전 충돌 시 최대 "+ retryMaxAttempt +"번 재시도");
        stopWatch.start();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();

        int updateQuantity = couponService.getOptimisticRemainingQuantity(couponId);

        System.out.println("쿠폰 초기 수량: " + initialQuantity);
        System.out.println("발급된 수량: " + (initialQuantity - updateQuantity));
        System.out.println("발급하지 못한 수량: " + (threadCount - (initialQuantity - updateQuantity)));
        System.out.println("스레드 개수: " + threadCount );
        System.out.println("버전 충돌 횟수: " + exceptions.size());
        System.out.println("재시도 총 횟수: " + retryTotalCounts.stream().collect(Collectors.summarizingInt(retryTotalCounts::get)).getSum());
        System.out.println(stopWatch.prettyPrint());

        assertThat(exceptions).hasSizeGreaterThan(0);
        assertThat(retryTotalCounts).hasSizeGreaterThan(0);
    }

//    @Test
    @DisplayName("스레드 10만개, 낙관적 락 버전 충돌된 트랜잭션 재시도")
    void optimisticLockIssueCouponVersionConflictRetryWith100000Thread() throws InterruptedException {
        int initialQuantity = couponService.getOptimisticRemainingQuantity(couponId);
        int threadCount = 100000;
        int retryMaxAttempt = 5;
        List<ObjectOptimisticLockingFailureException> exceptions = Collections.synchronizedList(new LinkedList<>());
        List<Integer> retryTotalCounts = Collections.synchronizedList(new ArrayList<>());

        Runnable issueCouponWithOptimisticLockRetryable = () -> {
            int retryCount = 0;
            boolean success = false;

            while (!success) {
                try {
                    couponService.issueCouponWithOptimisticReadLock(couponId);
                    success = true;
                } catch (ObjectOptimisticLockingFailureException e) {
                    exceptions.add(e);
                    retryCount++;
                    if (retryCount > retryMaxAttempt) {
                        break;
                    }
                }
            }
            retryTotalCounts.add(retryCount);
        };


        LinkedList<Thread> threads = new LinkedList<>();
        threads.addFirst(new Thread(() -> couponService.issueCouponWithOptimisticReadLock(couponId)));
        for (int i = 1; i < threadCount; i++) {
            threads.add(new Thread(issueCouponWithOptimisticLockRetryable));
        }

        StopWatch stopWatch = new StopWatch("낙관적 락 버전 충돌 시 최대 "+ retryMaxAttempt +"번 재시도");
        stopWatch.start();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        stopWatch.stop();

        int updateQuantity = couponService.getOptimisticRemainingQuantity(couponId);

        System.out.println("쿠폰 초기 수량: " + initialQuantity);
        System.out.println("발급된 수량: " + (initialQuantity - updateQuantity));
        System.out.println("발급하지 못한 수량: " + (threadCount - (initialQuantity - updateQuantity)));
        System.out.println("스레드 개수: " + threadCount );
        System.out.println("버전 충돌 횟수: " + exceptions.size());
        System.out.println("재시도 총 횟수: " + retryTotalCounts.stream().collect(Collectors.summarizingInt(retryTotalCounts::get)).getSum());
        System.out.println(stopWatch.prettyPrint());

        assertThat(exceptions).hasSizeGreaterThan(0);
        assertThat(retryTotalCounts).hasSizeGreaterThan(0);
    }


}
