package ru.yandextask.anotherdisk.dto.response;

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
public class SystemItemHistoryResponse {
    List<SystemItem> items;
}
