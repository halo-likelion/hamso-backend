package likelion.halo.hamso.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import likelion.halo.hamso.domain.type.Region1;
import likelion.halo.hamso.domain.type.Region2;
import likelion.halo.hamso.domain.type.Region3;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Table(name = "region")
public class AgriRegion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "region1")
    private Region1 region1; // 특별시, 광역시, 도

    @Enumerated(EnumType.STRING)
    @Column(name = "region2")
    private Region2 region2; // 시, 군, 구

    @Enumerated(EnumType.STRING)
    @Column(name = "region3")
    private Region3 region3; // 임대소
    public AgriRegion(String region1, String region2, String region3) {
        this.region1 = Region1.valueOf(region1);
        this.region2 = Region2.valueOf(region2);
        this.region3 = Region3.valueOf(region3);
    }
}
