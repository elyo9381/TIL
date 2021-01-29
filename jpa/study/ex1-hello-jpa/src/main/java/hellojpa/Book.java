package hellojpa;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B")// 이런식으로 DTYPE의 값을 줄수있다.
public class Book extends Item{

    private String author;
    private String isbn;
}