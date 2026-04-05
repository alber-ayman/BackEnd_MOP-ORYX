package com.example.demo.service.store;

import com.example.demo.models.ExportSupply;
import com.example.demo.repository.ExportSupplyRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExportSuppliesService {

    @Autowired
    ExportSupplyRepository supplyRepository;

    @Autowired
    SupplySerialService supplySerialService;

    public ResponseEntity<List<ExportSupply>> getAllSupplies() {
        List<ExportSupply> supplies = supplyRepository.findAll();
        return new ResponseEntity<>(supplies, HttpStatus.OK);
    }

    public Optional<ExportSupply> getSupplyById(Long id) {
        return supplyRepository.findAllById(id);
    }

    public ResponseEntity<ExportSupply> addNewSupply(ExportSupply supply, HttpServletRequest request) {
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("hh:mm:ss a");
        DateFormat formatter1 = new SimpleDateFormat("dd.MM.yy");
        GregorianCalendar gcalendar = new GregorianCalendar();
        supply.setSupplyNumber(String.valueOf(supplySerialService.getMaxImportSerial() + 1)
                .concat("/").concat(String.valueOf(gcalendar.get(Calendar.YEAR))));

        supply.setData(formatter1.format(dNow));
        supply.setTime(ft.format(dNow));
        supplySerialService.increaseImportSerial();
        return new ResponseEntity<>(supplyRepository.save(supply), HttpStatus.OK);
    }


}
