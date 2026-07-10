package com.example.demo.repository;

import com.example.demo.models.JobOrder;
import com.example.demo.models.PandsToJobOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOrderRepository extends JpaRepository<JobOrder,Long> {

    @Query(value = "SELECT MAX(j.number) FROM Job_Order j where project_profile_id = :projectCode and j.year = ( SELECT YEAR(NOW()) AS CurrentYear)", nativeQuery = true)
    Integer findMaxNumber(@Param("projectCode")Long projectCode);

    @Query(value = "SELECT MAX(j.id) FROM Job_Order j", nativeQuery = true)
    Long findMaxId();

    @Query(value = "SELECT * FROM Job_Order where project_profile_id = :projectCode ORDER BY id DESC LIMIT 1 ", nativeQuery = true)
    JobOrder findLastInserted(@Param("projectCode")Long projectCode);


    @Query(value = "SELECT * FROM Job_Order where project_code = :projectCode and inc_Number = :incNumber", nativeQuery = true)
    JobOrder getByProjectCodeAndIncNumber(@Param("projectCode")String projectCode,@Param("incNumber") int incNumber);

    @Query(value = "SELECT * FROM Job_Order where project_profile_id = :project_profile_id and work_order_header = :job_order_Number", nativeQuery = true)
    JobOrder getByProjectCodeAndJobOrderNumber(@Param("project_profile_id")Long projectCode,@Param("job_order_Number") String jobOrderNumber);


    List<JobOrder> getByProjectProfileId(Long id);

    JobOrder getByJobOrderNumber(String jobOrder);

    void deleteByProjectProfileId(Long id);

    @Query(value = "SELECT * FROM job_order where created_by = :userName order by id desc limit 1", nativeQuery = true)
    JobOrder getJobOrderByUser(@Param("userName") String userName);

    @Query(value = "SELECT * FROM job_order where approved = 0", nativeQuery = true)
    List<JobOrder> getPendingJobOrder();

    @Query(value = "SELECT * FROM job_order where manufacturing_manager = 1", nativeQuery = true)
    List<JobOrder> getPendingManufacturingJobOrder();

    @Query(value = "SELECT * FROM job_order where store_manager = 1", nativeQuery = true)
    List<JobOrder> getPendingStoreJobOrder();

    @Query(value = "SELECT * FROM job_order where purchasing_manager = 1", nativeQuery = true)
    List<JobOrder> getPendingPurchaseJobOrder();

    @Query(value = "SELECT * FROM job_order where created_by = :userName and reverted = 1", nativeQuery = true)
    List<JobOrder> getRevertedWorkOrdersByUser(@Param("userName") String userName);

    List<JobOrder> getByProjectCode(String projectCode);

    @Query(value = "SELECT * FROM job_order order BY project_code asc", nativeQuery = true)
    List<JobOrder> FindAllGroupByProject();
}
