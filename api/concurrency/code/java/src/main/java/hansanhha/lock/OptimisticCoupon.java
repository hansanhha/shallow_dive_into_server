package hansanhha.lock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Data
public class OptimisticCoupon {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    private int remainingQuantity;

    @Version
    private int version;

    public void decreaseRemainingQuantity() {
        remainingQuantity--;
    }
}
