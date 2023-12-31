package likelion.halo.hamso.dto.agriculture;

import likelion.halo.hamso.domain.AgriMachine;
import likelion.halo.hamso.domain.AgriRegion;
import likelion.halo.hamso.domain.type.AgriMachineType;
import likelion.halo.hamso.domain.type.Region1;
import likelion.halo.hamso.domain.type.Region2;
import likelion.halo.hamso.domain.type.Region3;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MachineInfoDto {

    private Long machineId; // 기계 종류 번호
    private AgriMachineType type; // 농기계 종류

    private Integer price; // 임대 가격

    private String content; // 농기계 설명

//    private Boolean reservePossible; // 예약 가능 여부

    private Region1 region1;
    private Region2 region2;
    private Region3 region3;

    private String imageUrl; // 농기계 설명

    private Long regionId;

    public MachineInfoDto(AgriMachine agriMachine) {
        this.machineId = agriMachine.getId();
        this.type = agriMachine.getType();
        this.price = agriMachine.getPrice();
        this.content = agriMachine.getContent();
        this.region1 = agriMachine.getRegion().getRegion1();
        this.region2 = agriMachine.getRegion().getRegion2();
        this.region3 = agriMachine.getRegion().getRegion3();
        this.imageUrl = agriMachine.getImageUrl();
        this.regionId = agriMachine.getRegion().getId();
    }
}
