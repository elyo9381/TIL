package hellojpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A")// 이런식으로 DTYPE의 값을 줄수있다.
public class Album extends Item{

    private String artist;
}