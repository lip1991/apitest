package restful.demo.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
@Getter
@AllArgsConstructor
public enum Gender {

    MAN("MAN", "남자"),
    WOMAN("WOMAN", "여자");

    private String code;
    private String codeName;

}
