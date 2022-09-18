package ru.yandextask.anotherdisk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandextask.anotherdisk.dto.response.SystemItemResponse;
import java.util.List;

public interface SystemItemRespRepository extends JpaRepository<SystemItemResponse,String> {

    SystemItemResponse findSystemItemResponseById (String id);
    List<SystemItemResponse> findSystemItemResponseByType(String type);
    List<SystemItemResponse> findSystemItemResponseByParentId (String parentId);


}
