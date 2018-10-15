package entity;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Person {

    private String id;
    private String name;
    private Integer age;

    @Override
    public String toString() {
        return "Person {" +
                "id=" +  id +
                ", name=" + name +
                ", age=" +  age +
                '}';
    }
}
