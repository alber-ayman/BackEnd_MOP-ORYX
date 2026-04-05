package com.example.demo.repository;

import com.example.demo.models.ExitJobOrder;
import com.example.demo.models.ExitProcessJobOrder;
import com.example.demo.models.PandsToJobOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExitProcessJobOrderRepository extends JpaRepository<ExitProcessJobOrder,Long> {
    @Query(value = "SELECT distinct(unit) FROM Exit_Process_Job_Order where project_code = :projectCode and job_order_id = :job_order_id", nativeQuery = true)
    List<String> jobOrdersByRawType(@Param("projectCode")String projectCode, @Param("job_order_id")String jobOrderCode);

    @Query(value = "SELECT * FROM Exit_Process_Job_Order where project_code = :projectCode and job_order_id = :job_order_id and unit = :unit order by thickness desc", nativeQuery = true)
    List<ExitProcessJobOrder> jobOrdersByUnit(@Param("projectCode")String projectCode,@Param("job_order_id")String jobOrderCode, @Param("unit")String unit);

    @Query(value = "SELECT  distinct(pand_code) FROM Exit_Process_Job_Order where project_code = :projectCode and job_order_id = :job_order_id" , nativeQuery = true)
    List<String> getJobOrderDetails(@Param("projectCode") String projectCode, @Param("job_order_id") String jobOrderID);

    @Query(value = "SELECT  sum(total) FROM Exit_Process_Job_Order where project_code = :projectCode and job_order_id = :job_order_id and pand_code = :pandCode " , nativeQuery = true)
    Double getTotalJobOrderForBill(@Param("projectCode") String projectCode, @Param("job_order_id") String jobOrderID, @Param("pandCode") String pandCode);


}
