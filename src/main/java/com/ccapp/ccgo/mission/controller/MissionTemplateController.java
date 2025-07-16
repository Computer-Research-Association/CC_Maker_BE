//package com.ccapp.ccgo.mission.controller;
//import com.ccapp.ccgo.mission.entity.MissionTemplate;
//import com.ccapp.ccgo.mission.service.MissionTemplateService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/missions")
//@RequiredArgsConstructor
//public class MissionTemplateController {
//
//    private final MissionTemplateService missionTemplateService;
//
//    @GetMapping("/score/{score}/six")
//    public ResponseEntity<List<MissionTemplate>> getSixMissionsByScore(@PathVariable int score) {
//        return ResponseEntity.ok(missionTemplateService.getSixMissionsByScore(score));
//    }
//
//    @PostMapping("/score/{score}/refresh")
//    public ResponseEntity<MissionTemplate> refreshMission(@PathVariable int score,
//                                                          @RequestBody List<Long> excludedIds) {
//        MissionTemplate mission = missionTemplateService.refreshMission(score, excludedIds);
//        return ResponseEntity.ok(mission);
//    }
//}
