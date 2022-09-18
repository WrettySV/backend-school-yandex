package ru.yandextask.anotherdisk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;


@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SystemItemImportRequest {

    private List<SystemItemImport> items;
    private String updateDate;

    public SystemItemImportRequest() {
    }
}
