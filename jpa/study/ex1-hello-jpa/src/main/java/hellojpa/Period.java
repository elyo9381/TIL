package hellojpa;

import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable // 값타입이라고 알려주는 애노테이션
public class Period {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 응집성 있는 메소드를 만들수있음.
//    public boolean isWord(){
//
//    }

    public Period() {
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}