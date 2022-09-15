package ru.yandextask.anotherdisk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;

public interface AnotherDiskService {
    ResponseEntity imports (String json) throws JsonProcessingException, ParseException;
    ResponseEntity delete (String id,  String date) throws JsonProcessingException;
    ResponseEntity nodes (String id) throws JsonProcessingException;
    ResponseEntity updates (String date);
    ResponseEntity history (String id, String dateStart, String dateEnd);


}
