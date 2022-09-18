package ru.yandextask.anotherdisk.dto.request;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;


@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SystemItemImport {

    private String id;
    private String url;
    private String parentId;
    private String type;
    private Integer size;

    public SystemItemImport() {
    }
}
