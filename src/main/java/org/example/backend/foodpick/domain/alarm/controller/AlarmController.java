package org.example.backend.foodpick.domain.alarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.alarm.dto.AlarmResponse;
import org.example.backend.foodpick.domain.alarm.dto.SendAlarmRequest;
import org.example.backend.foodpick.domain.alarm.service.AlarmService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getMyAlarms(
            @RequestHeader("Authorization") String token
    ) {
        return alarmService.getMyAlarms(token);
    }

    @PatchMapping("/{alarm_id}/read")
    public ResponseEntity<ApiResponse<String>> readAlarm(
            @RequestHeader("Authorization") String token,
            @PathVariable("alarm_id") Long alarmId
    ) {
        return alarmService.readAlarm(token, alarmId);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> readAllAlarms(
            @RequestHeader("Authorization") String token
    ) {
        return alarmService.readAllAlarms(token);
    }

    @DeleteMapping("/{alarm_id}/delete")
    public ResponseEntity<ApiResponse<String>> deleteAlarm(
            @RequestHeader("Authorization") String token,
            @PathVariable("alarm_id") Long alarmId
    ) {
        return alarmService.deleteAlarm(token, alarmId);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllMyAlarms(
            @RequestHeader("Authorization") String token
    ) {
        return alarmService.deleteAllMyAlarms(token);
    }
}
