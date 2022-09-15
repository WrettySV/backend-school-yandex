package ru.yandextask.anotherdisk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SystemItem {
    private String id;
    private String url;
    private String parentId;
    private String type;
    private Integer size;
    private String date;
}
