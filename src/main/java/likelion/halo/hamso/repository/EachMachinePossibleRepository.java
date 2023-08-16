package likelion.halo.hamso.repository;

import likelion.halo.hamso.domain.AgriPossible;
import likelion.halo.hamso.domain.EachMachinePossible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EachMachinePossibleRepository extends JpaRepository<EachMachinePossible, Long> {

    @Query("select p from EachMachine p where p.id=:eachMachineId")
    List<EachMachinePossible> findListById(Long eachMachineId);
}
