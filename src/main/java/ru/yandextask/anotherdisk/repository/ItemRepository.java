package ru.yandextask.anotherdisk.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import ru.yandextask.anotherdisk.entity.ItemEntity;

import java.util.List;
import java.util.function.Function;

public interface ItemRepository extends JpaRepository<ItemEntity,String> {

    ItemEntity findItemEntityById (String id);
    List<ItemEntity> findAllByType(String type);
    List<ItemEntity> findItemEntitiesByParentId (String parentId);


}
