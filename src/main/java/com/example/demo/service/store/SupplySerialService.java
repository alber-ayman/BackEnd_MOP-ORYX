package com.example.demo.service.store;

import com.example.demo.models.SupplySerial;
import com.example.demo.repository.SupplySerialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SupplySerialService {

    @Autowired
    SupplySerialRepository supplySerialRepository;

    public int getMaxImportSerial(){
        return supplySerialRepository.findMaxImportSerial();
    }

    public int getMaxExportSerial(){
    return  supplySerialRepository.findMaxExportSerial();
    }

    public void increaseImportSerial(){
        Optional<SupplySerial> supplySerial = supplySerialRepository.findById(1L);
        supplySerial.get().setImportSerial(supplySerial.get().getImportSerial() + 1);
        supplySerialRepository.save(supplySerial.get());
    }

    public void increaseExportSerial(){
        Optional<SupplySerial> supplySerial = supplySerialRepository.findById(1L);
        supplySerial.get().setExportSerial(supplySerial.get().getExportSerial() + 1);
        supplySerialRepository.save(supplySerial.get());
    }
}
