package hansanhha.lock;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Coupon {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    private int remainingQuantity;

    public void decreaseRemainingQuantity() {
        remainingQuantity--;
    }
}
