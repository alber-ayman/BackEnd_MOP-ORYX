package com.example.demo.service.workOrder;

import com.example.demo.models.Pand;
import com.example.demo.models.PreviewJobOrder;
import com.example.demo.repository.PreviewJobOrderRepository;
import com.example.demo.service.pand.PandsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
public class PreviewJobOrderService {

    @Autowired
    PandsService pandsService;

    @Autowired
    PreviewJobOrderRepository previewJobOrderRepository;

    @RequestScope
    public PreviewJobOrder savePreviewPandToJobOrder(PreviewJobOrder pandsToJobOrder) throws SQLException {

        Pand pand = pandsService.getPandByPandCode(pandsToJobOrder.getPandCode(), pandsToJobOrder.getProjectProfileId());

        double total;
        DecimalFormat df = new DecimalFormat("#.###");

        if (pandsToJobOrder.getUnit().equals("Square Meter")) {
            total = (Double.valueOf(pandsToJobOrder.getHeight()) * Double.valueOf(pandsToJobOrder.getWidth()) * Double.valueOf(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()))) / 10000;
        } else if (pandsToJobOrder.getUnit().equals("Longitudinal meter")) {
            total = (Double.valueOf(pandsToJobOrder.getHeight()) * Double.valueOf(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()))) / 100;
        } else {
            total = Double.valueOf(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()));
        }

        if (total > pand.getRestQuantity()) {
            pandsToJobOrder.setFlag(1);
            pandsToJobOrder.setMessage(" Quantity Exceeds The Main Quantity In Pand: " + pand.getPandCode());
            return pandsToJobOrder;
        }

        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");
        pandsToJobOrder.setJobOrderTime(ft.format(dNow).toString());

        long currentTimeMillis = System.currentTimeMillis();
        long leastSigBits = currentTimeMillis;
        long mostSigBits = Instant.now().getEpochSecond();

        UUID uuid = new UUID(mostSigBits, leastSigBits);

        pandsToJobOrder.setUniqueId(uuid.toString());
        pandsToJobOrder.setTotal(df.format(total));
        pandsToJobOrder.setMainTotal(df.format(total));
        pandsToJobOrder.setMainQuantity(pandsToJobOrder.getQuantity());
        pandsToJobOrder.setQuantity(pandsToJobOrder.getQuantity() * Double.valueOf(pandsToJobOrder.getRepetition()));


        previewJobOrderRepository.save(pandsToJobOrder);

        return pandsToJobOrder;
    }
}
