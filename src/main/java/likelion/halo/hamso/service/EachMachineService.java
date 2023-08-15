package likelion.halo.hamso.service;

import likelion.halo.hamso.domain.AgriMachine;
import likelion.halo.hamso.domain.AgriPossible;
import likelion.halo.hamso.domain.EachMachine;
import likelion.halo.hamso.domain.Reservation;
import likelion.halo.hamso.domain.type.AgriMachineType;
import likelion.halo.hamso.domain.type.ReservationStatus;
import likelion.halo.hamso.dto.agriculture.EachMachineDto;
import likelion.halo.hamso.dto.reservation.ReservationAdminInfoDto;
import likelion.halo.hamso.dto.reservation.ReservationLogDto;
import likelion.halo.hamso.dto.reservation.ReservationLogSpecificDto;
import likelion.halo.hamso.exception.NotEnoughCntException;
import likelion.halo.hamso.exception.NotFoundException;
import likelion.halo.hamso.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EachMachineService {
    private final AgriMachineRepository agriMachineRepository;
    private final AgriRegionRepository agriRegionRepository;
    private final ReservationRepository reservationRepository;
    private final PossibleRepository possibleRepository;
    private final MemberRepository memberRepository;
    private final EachMachineRepository eachMachineRepository;


    @Transactional
    public Long insertEachMachine(EachMachineDto eachMachineDto) {
        Long machineId = eachMachineDto.getMachineId();
        Optional<AgriMachine> oMachine = agriMachineRepository.findById(machineId);
        if(oMachine.isEmpty()) {
            throw new NotFoundException("해당 기계의 정보는 존재하지 않습니다!");
        }
        AgriMachine machine = oMachine.get();
        EachMachine eachMachine = new EachMachine();
        eachMachine.setMachine(machine);
        eachMachine.setEachMachinePossible(eachMachineDto.getEachMachinePossible());
        eachMachineRepository.save(eachMachine);
        addCnt(machineId);
        return eachMachine.getId();
    }

    @Transactional
    public void deleteEachMachine(Long eachMachineId) {
        Optional<EachMachine> oEachMachine = eachMachineRepository.findById(eachMachineId);
        if(oEachMachine.isEmpty()) {
            throw new NotFoundException("해당 기계의 정보는 존재하지 않습니다!");
        }
        EachMachine eachMachine = oEachMachine.get();
        eachMachineRepository.delete(eachMachine);
        removeCnt(eachMachine.getMachine().getId());
    }

    @Transactional
    public Integer removeCnt(Long machineId) {
        Optional<AgriMachine> oMachine = agriMachineRepository.findById(machineId);
        if(oMachine.isEmpty()) {
            throw new NotFoundException("해당 기계의 정보는 존재하지 않습니다!");
        }
        AgriMachine machine = oMachine.get();
        int cnt = machine.getOriCnt();
        cnt--;
        if (cnt < 0) {
            throw new NotEnoughCntException("더 이상의 삭제는 불가능합니다.");
        }
        List<AgriPossible> agriPossibles = possibleRepository.findByMachineId(machineId);
        if (cnt <= 0) {
            for(AgriPossible agriPossible:agriPossibles) {
                if(agriPossible.getReservePossible()) {
                    agriPossible.setReservePossible(false);
                }
            }
            return 0;
        }
        machine.setOriCnt(cnt);
        return machine.getOriCnt();
    }

    @Transactional
    public Integer addCnt(Long machineId) {
        Optional<AgriMachine> oMachine = agriMachineRepository.findById(machineId);
        if(oMachine.isEmpty()) {
            throw new NotFoundException("해당 기계의 정보는 존재하지 않습니다!");
        }
        AgriMachine machine = oMachine.get();
        int cnt = machine.getOriCnt();
        cnt++;

        List<AgriPossible> agriPossibles = possibleRepository.findByMachineId(machineId);
        if (cnt-1 == 0) {
            for(AgriPossible agriPossible:agriPossibles) {
                if(!agriPossible.getReservePossible()){
                    agriPossible.setReservePossible(true);
                }
            }
        }
        machine.setOriCnt(cnt);
        return machine.getOriCnt();
    }
}