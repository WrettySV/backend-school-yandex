package ru.yandextask.anotherdisk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SystemItemResponse {
    private String id;
    private String url;
    private String parentId;
    private String type;
    private Integer size;
    private String date;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SystemItemResponse> children;
}
