package ru.yandextask.anotherdisk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandextask.anotherdisk.dto.request.SystemItemImport;
import ru.yandextask.anotherdisk.dto.request.SystemItemImportRequest;
import ru.yandextask.anotherdisk.dto.response.Status;
import ru.yandextask.anotherdisk.dto.response.SystemItemResponse;
import ru.yandextask.anotherdisk.entity.ItemEntity;
import ru.yandextask.anotherdisk.repository.ItemRepository;
import ru.yandextask.anotherdisk.repository.SystemItemRespRepository;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnotherDiskServiceImpl implements AnotherDiskService{

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    SystemItemRespRepository systemItemRespRepository;
    @Autowired
    UtilsService utilsService;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    private ResponseEntity sendStatus (Integer code, String message) throws JsonProcessingException {
        Status error = new Status(code, message);
        String errorJson = objectMapper.writeValueAsString(error);
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(errorJson);
    }

    private static boolean isCorrectId(String id){
        return !(id == null && !id.contains("?") && id.contains("&") && id.contains("%"));
    }
    private Date convertTime(String dateStr) {
        Date dateT;
        try {
            dateT = sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
        return dateT;
    }

    @Override
    public ResponseEntity imports(String json) throws JsonProcessingException {

        try {
            SystemItemImportRequest systemItemImportRequest = objectMapper.readValue(json, SystemItemImportRequest.class);
            List<SystemItemImport> items = systemItemImportRequest.getItems();
            List<ItemEntity> itemEntities = new ArrayList<>();

            Date updateDate = convertTime(systemItemImportRequest.getUpdateDate());
            if (updateDate == null)
                return sendStatus(400, "Validation Failed. Invalid date"); //дата обрабатывается согласно ISO 8601
            if (items.stream().map(e -> e.getId()).distinct().collect(Collectors.toList()).size()
                    != items.size())
                return sendStatus(400, "Validation Failed. Not distinct ids "); // в одном запросе не может быть двух элементов с одинаковым id
            for (SystemItemImport item : items) {
                if (item.getId() == null)
                    return sendStatus(400, "Validation Failed. поле id не может быть равно null");//поле id не может быть равно null
                if (item.getType().equals("FILE") && item.getSize() <= 0)
                    return sendStatus(400, "Validation Failed. поле size для файлов всегда должно быть больше 0"); //поле size для файлов всегда должно быть больше 0
                if (item.getType().equals("FILE") && item.getUrl().length() > 255)
                    return sendStatus(400, "Validation Failed. размер поля url при импорте файла всегда должен быть меньше либо равным 255"); //размер поля url при импорте файла всегда должен быть меньше либо равным 255
                if (item.getType().equals("FOLDER") && item.getSize() != null)
                    return sendStatus(400, "Validation Failed. поле size при импорте папки всегда должно быть равно null"); //поле size при импорте папки всегда должно быть равно null
                if (item.getType().equals("FOLDER") && item.getUrl() != null)
                    return sendStatus(400, "Validation Failed. поле url при импорте папки всегда должно быть равно null"); //поле url при импорте папки всегда должно быть равно null
                if (itemRepository.findItemEntityById(item.getId()) != null &&
                        !itemRepository.findItemEntityById(item.getId()).getType().equals(item.getType())) // Изменение типа элемента с папки на файл и с файла на папку не допускается.
                    return sendStatus(400, "Validation Failed. Изменение типа элемента с папки на файл и с файла на папку не допускается.");

                ItemEntity itemEntity =
                        new ItemEntity(item.getId(), item.getUrl(), updateDate, item.getParentId(), item.getType(), item.getSize());
                itemEntities.add(itemEntity);

            }
            itemRepository.saveAll(itemEntities);

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
        }catch (Exception e) {
            return sendStatus(400, "Validation Failed. Exception saving ");
        }

        return sendStatus(200, "Successfully imported");

    }

    @Override
    public ResponseEntity delete(String id, String date) throws JsonProcessingException {
        if (!isCorrectId(id)) // || !isIsoDate(date))
            return sendStatus(400, "Validation Failed");
        try { //checking id specific characters, date for update the folder contains current file
            ItemEntity itemFound = itemRepository.findItemEntityById(id);
            if (itemFound!= null) {
                if (itemFound.getType().equals("FILE")){
                    itemRepository.deleteById(id);
                    if (itemFound.getParentId() != null)
                    {
                        ItemEntity folderOfItemFound = itemRepository.findItemEntityById(itemFound.getParentId());
                        folderOfItemFound.setUpdateDate(convertTime(date));
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
            SystemItemResponse itemResp = systemItemRespRepository.findSystemItemResponseById(id);

            return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON)
                    .body(utilsService.decerializeObject(itemResp));

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
