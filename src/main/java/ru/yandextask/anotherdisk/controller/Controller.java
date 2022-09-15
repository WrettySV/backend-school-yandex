package ru.yandextask.anotherdisk.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.yandextask.anotherdisk.dto.response.SystemItemHistoryResponse;
import ru.yandextask.anotherdisk.dto.response.SystemItemResponse;
import ru.yandextask.anotherdisk.service.AnotherDiskServiceImpl;

import java.text.ParseException;

@RestController
@RequestMapping
public class Controller {

    @Autowired
    private AnotherDiskServiceImpl service;

    @PostMapping(path = "/imports")
    public ResponseEntity imports(@RequestBody @NotNull String json) throws JsonProcessingException, ParseException {
        return service.imports(json);
    }

    @DeleteMapping(path = "/delete/{id},{date}")
    @Transactional
    public ResponseEntity delete(@PathVariable("id") String id,
                                 @RequestParam(value="date", required=true) String date) throws JsonProcessingException {
        return service.delete(id, date);
    }

    @GetMapping(path = "/nodes/{id}")
    public ResponseEntity nodes(@PathVariable("id") String id) throws JsonProcessingException {
        return service.nodes(id);
    }

    @GetMapping(path = "/updates")
    public ResponseEntity updates(@RequestParam(value="date", required=true) String date) throws JsonProcessingException {
        return service.updates(date);
    }

    @GetMapping(path = "/node/{id}/history")
    public ResponseEntity node(@PathVariable("id") String id,
                               @RequestParam(value="dateStart", required=false) String dateStart,
                               @RequestParam(value="dateEnd", required=false) String dateEnd) throws JsonProcessingException {
        return service.history(id, dateStart, dateEnd);
    }
}
