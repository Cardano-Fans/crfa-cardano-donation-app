package crfa.app.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class Entity {

    String name;

    String address;

}
