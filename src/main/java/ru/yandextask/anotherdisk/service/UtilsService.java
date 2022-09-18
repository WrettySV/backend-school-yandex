package ru.yandextask.anotherdisk.service;

import ru.yandextask.anotherdisk.dto.response.SystemItemResponse;
import ru.yandextask.anotherdisk.entity.ItemEntity;

import java.util.List;

public interface UtilsService {

    List<ItemEntity> getAllChildrenCascadeFromDB(ItemEntity item);
    String decerializeObject(Object value);
}
