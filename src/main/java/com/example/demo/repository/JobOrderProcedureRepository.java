package com.example.demo.repository;

import com.example.demo.models.PandsToJobOrder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;

@Repository
public class JobOrderProcedureRepository {

    private final SimpleJdbcCall updateJobOrderCall;

    public JobOrderProcedureRepository(DataSource dataSource) {
        this.updateJobOrderCall = new SimpleJdbcCall(dataSource)
                .withProcedureName("sp_update_bands_job_order");
        // .withoutProcedureColumnMetaDataAccess() // uncomment if your DB user
        //   lacks permission to read INFORMATION_SCHEMA (common in locked-down prod DBs)
    }

    public ProcedureResult updateJobOrder(Long id, PandsToJobOrder updatedJobOrder, String changedBy) {

        SqlParameterSource in = new MapSqlParameterSource()
                .addValue("p_id", updatedJobOrder.getId())
                .addValue("p_project_code", updatedJobOrder.getProjectCode())
                .addValue("p_project_name", updatedJobOrder.getProjectName())
                .addValue("p_engineer_name", updatedJobOrder.getEngineerName())
                .addValue("p_job_order_type", updatedJobOrder.getJobOrderType())
                .addValue("p_manufacturing_code", updatedJobOrder.getManufacturingCode())
                .addValue("p_pand_code", updatedJobOrder.getPandCode())
                .addValue("p_description", updatedJobOrder.getDescription())
                .addValue("p_manufacturing", updatedJobOrder.getManufacturing())
                .addValue("p_raw_type", updatedJobOrder.getRawType())
                .addValue("p_raw_used", updatedJobOrder.getRawUsed())
                .addValue("p_finish_type", updatedJobOrder.getFinishType())
                .addValue("p_thickness", updatedJobOrder.getThickness())
                .addValue("p_block_number", updatedJobOrder.getBlockNumber())
                .addValue("p_floor", updatedJobOrder.getFloor())
                .addValue("p_unit", updatedJobOrder.getUnit())
                .addValue("p_additional_description", updatedJobOrder.getAdditionalDescription())
                .addValue("p_height", updatedJobOrder.getHeight())
                .addValue("p_width", updatedJobOrder.getWidth())
                .addValue("p_repetition", updatedJobOrder.getRepetition())
                .addValue("p_main_quantity", updatedJobOrder.getMainQuantity())
                .addValue("p_installation_area", updatedJobOrder.getInstallationArea())
                .addValue("p_project_profile_id", updatedJobOrder.getProjectProfileId())
                .addValue("p_changed_by", changedBy);

        Map<String, Object> out = updateJobOrderCall.execute(in);

        int flag = ((Number) out.get("p_out_flag")).intValue();
        String message = (String) out.get("p_out_message");

        return new ProcedureResult(flag, message);
    }

    public record ProcedureResult(int flag, String message) {
        public boolean isError() {
            return flag == 1;
        }
    }
}
