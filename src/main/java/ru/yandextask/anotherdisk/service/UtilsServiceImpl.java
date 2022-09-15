package ru.yandextask.anotherdisk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandextask.anotherdisk.entity.ItemEntity;
import ru.yandextask.anotherdisk.repository.ItemRepository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class UtilsServiceImpl implements UtilsService{
    @Autowired
    ItemRepository itemRepository;
    @Override
    public List<ItemEntity> getAllChildrenCascadeFromDB(ItemEntity item) {
        String id = item.getId();
        List<ItemEntity> allChildrenItems = new ArrayList<>();
        List<ItemEntity> childrenItemsTemp = itemRepository.findItemEntitiesByParentId(id);
        LinkedList<List<ItemEntity>> linkedListOfAllCHildren = new LinkedList<>();
        if (childrenItemsTemp != null){
            linkedListOfAllCHildren.addLast(childrenItemsTemp);
        }
        while(!linkedListOfAllCHildren.isEmpty()){
            for (ItemEntity itemChild : linkedListOfAllCHildren.pollFirst()){
                childrenItemsTemp = itemRepository.findItemEntitiesByParentId(itemChild.getId());
                if (childrenItemsTemp != null) {
                    linkedListOfAllCHildren.addLast(itemRepository.findItemEntitiesByParentId(itemChild.getId()));
                    allChildrenItems.addAll(childrenItemsTemp);
                }
            }
        }
        return allChildrenItems;
    }
    public String decerializeObject(Object value){
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try{
            json = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error(e.getOriginalMessage());
        }
        return json;
    }
}
