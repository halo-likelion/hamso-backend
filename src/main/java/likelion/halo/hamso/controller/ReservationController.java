package likelion.halo.hamso.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import likelion.halo.hamso.argumentresolver.Login;
import likelion.halo.hamso.domain.AgriMachine;
import likelion.halo.hamso.domain.EachMachine;
import likelion.halo.hamso.domain.Member;
import likelion.halo.hamso.domain.Reservation;
import likelion.halo.hamso.domain.type.ReservationStatus;
import likelion.halo.hamso.dto.agriculture.RegionMachineDto;
import likelion.halo.hamso.dto.alert.MessageDto;
import likelion.halo.hamso.dto.alert.SmsResponseDto;
import likelion.halo.hamso.dto.reservation.*;
import likelion.halo.hamso.exception.MemberDuplicateException;
import likelion.halo.hamso.exception.NotAvailableReserveException;
import likelion.halo.hamso.exception.NotFoundException;
import likelion.halo.hamso.exception.NotLoginException;
import likelion.halo.hamso.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reserve")
public class ReservationController {
    private final ReservationService reservationService;
    private final AgricultureService agricultureService;
    private final MemberService memberService;
    private final SmsService smsService;
    private final EachMachineService eachMachineService;

    @PostMapping("/check-possible")
    public ResponseEntity<Boolean> checkReservePossible(@RequestBody ReservationCheckDto reservationCheckDto) {
        LocalDateTime wantTime = LocalDateTime.of(reservationCheckDto.getYear(), reservationCheckDto.getMonth(), reservationCheckDto.getDay(), 0, 0);
        Boolean possibleCheck = reservationService.checkReservePossible(reservationCheckDto.getMachineType(), reservationCheckDto.getRegionId(), wantTime);
        return new ResponseEntity<>(possibleCheck, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Long> makeReservation(@RequestBody ReservationInfoDto reservationInfo,
                                                @Login Member loginMember) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        if(loginMember == null) {
            throw new NotLoginException("로그인이 안되어 있는 상태입니다.");
        }
        log.info("loginMember = {}", loginMember);

        Long machineId = reservationInfo.getMachineId();

        LocalDateTime wantTime = reservationInfo.getWantTime();
        if(wantTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("오늘보다 이전 날짜의 예약은 불가합니다.");
        }
        AgriMachine machine = agricultureService.findByMachineIdReal(machineId);

        Integer reserveDayCnt = reservationInfo.getReserveDayCnt();

        if(wantTime.isBefore(LocalDateTime.now())) {
            throw new NotAvailableReserveException("예약이 불가능합니다.");
        }

        Long reservationId = 0L;

        LocalDateTime endTime = wantTime.plusDays(reserveDayCnt - 1);

        // 예약할 날짜를 보내줬을 때 원래 있던 예약과 겹치는지?
        log.info("checkDuplicateReservation:  예약할 날짜를 보내줬을 때 원래 있던 예약과 겹치는지?");
        // 머신 타입으로 예약 가능하게 변경하기!
        if(!reservationService.checkReserveAllDayPossible(reservationInfo.getMachineId(), wantTime, reserveDayCnt)) {
            throw new NotAvailableReserveException("예약이 불가능합니다.");
        }
        // 예약 저장
        reservationInfo.setWantTime(wantTime);
        reservationInfo.setEndTime(endTime);

        Reservation reservation = Reservation.createReservation(reservationInfo, loginMember, machine);
        log.info("reservation = {}", reservation);
        reservationService.makeReservation(reservation);
        // remove Cnt
        reservationService.removeCnt(machineId, wantTime, reserveDayCnt);
        reservationId = reservation.getId();


        // reservation message sending
        MessageDto messageDto = new MessageDto();
        messageDto.setTo(loginMember.getPhoneNo());

        int year = wantTime.getYear();
        Month month = wantTime.getMonth();
        int day = wantTime.getDayOfMonth();

        if(reserveDayCnt == 1) {
            messageDto.setContent("<렛츠-농사> " + loginMember.getName() +"님 "+ machine.getType()+"이(가) [" +year + "년" + month.getValue() + "월" + day + "일" +"]에 예약 신청에 성공하셨습니다.");
            SmsResponseDto response1 = smsService.sendSms(messageDto);
            log.info("message log1 = {}", response1);
            messageDto.setContent("예약 확정을 위해서 <계좌번호>로 예약 하신 날짜 당일까지 입금 부탁드립니다 ♡");
            SmsResponseDto response2 = smsService.sendSms(messageDto);
            log.info("message log2 = {}", response2);
        } else {
            messageDto.setContent("<렛츠-농사> " + loginMember.getName() +"님 [" +year + "년" + month.getValue() + "월" + day + "일~" +year + "년" + month.getValue() + "월" + (day+reserveDayCnt-1) + "일"+"]에 예약 신청에 성공하셨습니다.");
            SmsResponseDto response1 = smsService.sendSms(messageDto);
            log.info("message log1 = {}", response1);
            messageDto.setContent("예약 확정을 위해서 <계좌번호>로 예약 하신 날짜 당일까지 입금 부탁드립니다 ♡");
            SmsResponseDto response2 = smsService.sendSms(messageDto);
            log.info("message log2 = {}", response2);
        }
        return new ResponseEntity<>(reservationId, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ReservationLogDto>> getReservationLogList(@Login Member loginMember) {
        List<ReservationLogDto> reservationLogDtoList = reservationService.getReservationLogList(loginMember.getLoginId());
        return new ResponseEntity<>(reservationLogDtoList, HttpStatus.OK);
    }

    @GetMapping("/list-specific/{reservationId}")
    public ResponseEntity<ReservationLogSpecificDto> getReservationLogSpecificList(@Login Member loginMember, @PathVariable("reservationId") Long reservationId) {
        ReservationLogSpecificDto reservationLogDto = reservationService.getReservationLogSpecificList(loginMember.getLoginId(), reservationId);
        return new ResponseEntity<>(reservationLogDto, HttpStatus.OK);
    }

    @PostMapping("/possible/month")
    public ResponseEntity<Integer[]> possibleMonthArray(@RequestBody RegionMachineDto regionMachineDto) {
        return new ResponseEntity<>(reservationService.getPossibleMonthArray(regionMachineDto.getMachineId()), HttpStatus.OK);
    }

    @PutMapping("/cancel/{reservationId}")
    public ResponseEntity<ReservationStatus> cancelReservation(@Login Member loginMember, @PathVariable("reservationId") Long reservationId) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        Reservation reservation = reservationService.findReservationById(reservationId);
        LocalDateTime wantTime = reservation.getWantTime();
        int year = wantTime.getYear();
        Month month = wantTime.getMonth();
        int day = wantTime.getDayOfMonth();

        ReservationUpdateDto reservationUpdateDto = new ReservationUpdateDto(reservationId, ReservationStatus.CANCELED, reservation.getEachMachine().getId());
        ReservationStatus reservationStatus = reservationService.updateReservationStatus(reservationUpdateDto);

        MessageDto messageDto = new MessageDto();
        messageDto.setTo(loginMember.getPhoneNo());
        messageDto.setContent("<렛츠-농사> " + loginMember.getName() +"님 "+ reservation.getAgriMachine().getType()+"의 예약 신청이 취소되셨습니다.");

        SmsResponseDto response = smsService.sendSms(messageDto);
        log.info("message log = {}", response);
        return new ResponseEntity<>(reservationStatus, HttpStatus.OK);
    }

    @PostMapping("/assign/each-machine")
    public ResponseEntity<Long> assignEachMachine(@RequestBody ReservationAssignEachMachine reservationAssignEachMachine) {
        Long eachMachineId = reservationService.assignEachMachine(reservationAssignEachMachine);
        return new ResponseEntity<>(eachMachineId, HttpStatus.OK);
    }

}
