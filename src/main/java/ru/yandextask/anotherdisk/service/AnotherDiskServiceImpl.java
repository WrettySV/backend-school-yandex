package ru.yandextask.anotherdisk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandextask.anotherdisk.dto.request.SystemItemImport;
import ru.yandextask.anotherdisk.dto.request.SystemItemImportRequest;
import ru.yandextask.anotherdisk.dto.response.Status;
import ru.yandextask.anotherdisk.dto.response.SystemItemResponse;
import ru.yandextask.anotherdisk.entity.ItemEntity;
import ru.yandextask.anotherdisk.repository.ItemRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnotherDiskServiceImpl implements AnotherDiskService{

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    UtilsService utilsService;

    private ResponseEntity sendStatus (Integer code, String message) throws JsonProcessingException {
        Status error = new Status(code, message);
        String errorJson = objectMapper.writeValueAsString(error);
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(errorJson);
    }
    private static boolean isIsoDate(String date) {
        try {
            Instant.from(DateTimeFormatter.ISO_INSTANT.parse(date));
            return true;
        } catch (DateTimeParseException e) {
        }
        return false;
    }
    private static boolean isCorrectId(String id){
        return !(id == null && !id.contains("?") && id.contains("&") && id.contains("%"));
    }

    @Override
    public ResponseEntity imports(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SystemItemImportRequest systemItemImportRequest = objectMapper.readValue(json,SystemItemImportRequest.class);
        List<SystemItemImport> items = systemItemImportRequest.getItems();
        List<ItemEntity> itemEntities = new ArrayList<>();
        try {
            Date updateDate = sdf.parse(systemItemImportRequest.getUpdateDate());
            if (isIsoDate(systemItemImportRequest.getUpdateDate()))
                return sendStatus(400, "Validation Failed"); //дата обрабатывается согласно ISO 8601
            if (items.stream().map(e -> e.getId()).distinct().collect(Collectors.toList()).size()
                    != items.size())
                return sendStatus(400, "Validation Failed"); // в одном запросе не может быть двух элементов с одинаковым id
            for (SystemItemImport item : items) {
                if (item.getId() == null //поле id не может быть равно null
                        || item.getType().equals("FILE") && item.getSize() > 0 //поле size для файлов всегда должно быть больше 0
                        || item.getType().equals("FILE") && item.getUrl().length() <= 255 //размер поля url при импорте файла всегда должен быть меньше либо равным 255
                        || item.getType().equals("FOLDER") && item.getSize() != null //поле size при импорте папки всегда должно быть равно null
                        || item.getType().equals("FOLDER") && item.getUrl() != null //поле url при импорте папки всегда должно быть равно null
                        || !itemRepository.findItemEntityById(item.getId()).getType().equals(item.getType()) // Изменение типа элемента с папки на файл и с файла на папку не допускается.
                ) return sendStatus(400, "Validation Failed");
                else {
                    ItemEntity itemEntity =
                            new ItemEntity(item.getId(), item.getUrl(), updateDate, item.getParentId(), item.getType(), item.getSize());
                    itemEntities.add(itemEntity);
                }
            }

            List<ItemEntity> foldersDB = itemRepository.findAllByType("FOLDER");
            List<ItemEntity> foldersImporting = itemEntities.stream().filter(entry -> entry.getType().equals("FOLDER"))
                    .collect(Collectors.toList());
            List<ItemEntity> filesImporting = itemEntities.stream().filter(entry -> entry.getType().equals("FILES"))
                    .collect(Collectors.toList());

            List<String> parentIdAllItemlist = itemEntities.stream().map(e -> e.getParentId()).collect(Collectors.toList());
            List<String> parentIdFolderlist = foldersImporting.stream().map(e -> e.getParentId()).collect(Collectors.toList());

            List<ItemEntity> foldersDBjustUpdaedNotImporting =
                    foldersDB.stream().filter(f -> parentIdAllItemlist.contains(f.getId())
                            || !parentIdFolderlist.contains(f.getId())).collect(Collectors.toList());


            foldersDBjustUpdaedNotImporting.forEach(folder -> { //updating date for folders who is a parent of imprting items
                folder.setUpdateDate(updateDate);
                itemRepository.save(folder);
            });

            List<String> listIdOfFoldersImporting = foldersImporting.stream().map(e -> e.getId()).collect(Collectors.toList());
            List<String> listIdOfFoldersDBjustUpdaedNotImporting = foldersDBjustUpdaedNotImporting.stream().map(e -> e.getId()).collect(Collectors.toList());
            List<String> listIdsforUpdate = new ArrayList<>();
            listIdsforUpdate.addAll(listIdOfFoldersImporting);
            listIdsforUpdate.addAll(listIdOfFoldersDBjustUpdaedNotImporting);


            listIdsforUpdate.forEach(fId -> {
                ItemEntity folder = itemRepository.findItemEntityById(fId);
                Integer size = utilsService.getAllChildrenCascadeFromDB(folder).stream()
                        .filter(child -> child.getType().equals("FILE"))
                        .map(file -> file.getSize())
                        .mapToInt(i -> i)
                        .sum();
                folder.setSize(size);
                itemRepository.save(folder);
            });

            itemRepository.saveAll(filesImporting);

            return sendStatus(200, "Successfully imported");
        }catch (Exception e) {
            return sendStatus(400, "Validation Failed");
        }
    }

    @Override
    public ResponseEntity delete(String id, String date) throws JsonProcessingException {
        if (!isCorrectId(id) || !isIsoDate(date)) return sendStatus(400, "Validation Failed");
        try { //checking id specific characters, date for update the folder contains current file
            ItemEntity itemFound = itemRepository.findItemEntityById(id);
            if (itemFound!= null) {
                if (itemFound.getType().equals("FILE")){
                    itemRepository.deleteById(id);
                    if (itemFound.getParentId() != null)
                    {
                        ItemEntity folderOfItemFound = itemRepository.findItemEntityById(itemFound.getParentId());
                        folderOfItemFound.setUpdateDate(sdf.parse(date));
                        itemRepository.save(folderOfItemFound);
                    }
                    return sendStatus(200, "Deleted successfully");
                }
                else{
                    List<ItemEntity> allChildrenItems = new ArrayList<>();
                    List<ItemEntity> childrenItemsTemp = itemRepository.findItemEntitiesByParentId(id);
                    LinkedList<List<ItemEntity>> linkedListOfAllCHildren = new LinkedList<>();
                    if (childrenItemsTemp != null){
                        linkedListOfAllCHildren.addLast(childrenItemsTemp);
                    }
                    else{
                        itemRepository.deleteById(id);
                        return sendStatus(200, "Deleted successfully");
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
                    List<String> listIdForDelete = allChildrenItems.stream().map(ItemEntity::getId)
                            .collect(Collectors.toList());
                    listIdForDelete.add(id);
                    for (String idForDelete : listIdForDelete){
                        itemRepository.deleteById(idForDelete);
                    }
                    return sendStatus(200, "Deleted successfully");
                }
            } else {
                return sendStatus(404, "Item not found");
            }
        } catch (Exception e) {
            return sendStatus(400, "Validation Failed");
        }
    }

    @Override
    public ResponseEntity nodes(String id) throws JsonProcessingException {
        if (!isCorrectId(id)) return sendStatus(400, "Validation Failed");

        try{
            ItemEntity item = itemRepository.findItemEntityById(id);
            if (item == null) return sendStatus(404,"Item not found");
            else{
                SystemItemResponse systemItemResponse = new SystemItemResponse(id, item.getUrl(),
                        item.getParentId(), item.getType(), item.getSize(), sdf.format(item.getUpdateDate()), null);


                List<ItemEntity> childrenDB = utilsService.getAllChildrenCascadeFromDB(item);
                List<ItemEntity> childrenFiles  = childrenDB.stream().filter(child -> child.getType().equals("FILE"))
                        .collect(Collectors.toList());
                LinkedList<ItemEntity> childrenFolders  = new LinkedList<>(childrenDB.stream().filter(child -> child.getType().equals("FOLDER"))
                        .collect(Collectors.toList()));


                if (childrenFiles != null || !childrenFiles.isEmpty()){
                    childrenFiles.stream().forEach(file ->{
                    List<SystemItemResponse> kids = systemItemResponse.getChildren();
                    kids.add(new SystemItemResponse(file.getId(), file.getUrl(), file.getParentId(), file.getType(),
                            file.getSize(), sdf.format(file.getUpdateDate()), null));
                    systemItemResponse.setChildren(kids);
                    });

                }
                return sendStatus(200, utilsService.decerializeObject(systemItemResponse));
            }
        } catch (Exception e) {
            return sendStatus(400, "Validation Failed");
        }
    }

    @Override
    public ResponseEntity updates(String date) {
        return null;
    }

    @Override
    public ResponseEntity history(String id, String dateStart, String dateEnd) {
        return null;
    }
}


//        LinkedList<Map<String, List<ItemEntity>>> linkedListofGroups = new LinkedList<>();
//        linkedListofGroups.addLast(itemEnityGroups);
//        while (!linkedListofGroups.isEmpty()){
//            itemEnityGroups = linkedListofGroups.pollFirst();
//            for (Map.Entry<String, List<ItemEntity>> entry : itemEnityGroups.entrySet()){
//                itemEntities.stream().filter(item -> item.getId().equals(entry.getKey()))
//                        .forEach(item -> item.setChildren(entry.getValue()));
//                linkedListofGroups.addLast(entry.getValue().stream()
//                        .collect(Collectors.groupingBy(ItemEntity::getParentId)));
//
//            }
//        }
//        foldersImporting.stream().forEach(folder -> {
//                for (Map.Entry<String, List<ItemEntity>> entry : itemEnityGroups.entrySet()){
//        if (folder.getId().equals(entry.getKey())){
//        folder.setChildren(entry.getValue());
//        }
//        }
//        });

//    Map<String, List<ItemEntity>> itemEnityGroups = foldersImporting
//            .stream()
//            .filter(itemEntity -> itemEntity.getParentId() != null)
//            .collect(Collectors.groupingBy(ItemEntity::getParentId));