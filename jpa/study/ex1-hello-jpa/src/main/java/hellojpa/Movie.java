package hellojpa;

import javax.persistence.*;

@Entity
@DiscriminatorValue("M")// 이런식으로 DTYPE의 값을 줄수있다.
public class Movie extends Item{

    private String director;
    private String actor;

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }
}