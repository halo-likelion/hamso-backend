package likelion.halo.hamso.dto.reservation;

import likelion.halo.hamso.domain.EachMachine;
import likelion.halo.hamso.domain.type.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationInfoDto {
    private LocalDateTime wantTime; // 예약 원하는 날짜
    private LocalDateTime endTime; // 예약 원하는 날짜
    private Long machineId; // 빌리기 원하는 농기계 아이디
    private String workType; // 작업 종류
    private Double workload; // 작업량

    private Integer reserveDayCnt; // 연속 날짜
}
