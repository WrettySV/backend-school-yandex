package ru.yandextask.anotherdisk.dto.request;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.*;


@Slf4j
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Validated
public class SystemItemImport {

    private String id;
    private String url;
    private String parentId;
    private String type;
    private Integer size;
}
