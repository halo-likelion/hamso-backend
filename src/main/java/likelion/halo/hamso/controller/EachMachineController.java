package likelion.halo.hamso.controller;

import likelion.halo.hamso.dto.agriculture.*;
import likelion.halo.hamso.dto.member.MemberDto;
import likelion.halo.hamso.service.AgricultureService;
import likelion.halo.hamso.service.EachMachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/each")
public class EachMachineController {
    private final AgricultureService agricultureService;
    private final EachMachineService eachMachineService;

    @PostMapping("/insert")
    public ResponseEntity<Long> insertEachMachine(@RequestBody EachMachineDto eachMachineDto) {
        Long eachMachine = eachMachineService.insertEachMachine(eachMachineDto);
        return new ResponseEntity<>(eachMachine, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteEachMachine(@RequestParam Long eachMachineId) {
        eachMachineService.deleteEachMachine(eachMachineId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
