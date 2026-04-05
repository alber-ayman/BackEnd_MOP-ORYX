package com.example.demo.service.pand;

import com.example.demo.models.*;
import com.example.demo.repository.*;
import com.example.demo.service.ChangeHistoryLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class PandHistoryService {
    @Autowired
    PandHistoryRepository pandHistoryRepository;

    @Autowired
    PandsToJobOrderRepository pandsToJobOrderRepository;

    @Autowired
    PandsRepository pandsRepository;

    @Autowired
    private ChangeHistoryLog changeHistoryLog;

    @Autowired
    RawTypeRepository rawTypeRepository;

    public ResponseEntity<List<PandHistory>> getAllPandHistory(Long id) {
        List<PandHistory> pandHistories = pandHistoryRepository.getByPandId(id);
        return new ResponseEntity<>(pandHistories, HttpStatus.OK);
    }

    public ResponseEntity<PandHistory> addNewPandHistory(PandHistory pandHistory, HttpServletRequest request) {
        try {
            Optional<Pand> pand = pandsRepository.findById(pandHistory.getPandId());
            pand.get().setRestQuantity(pand.get().getRestQuantity() + pandHistory.getAdditionalQuantity());
            pand.get().setMockQuantity(pand.get().getMockQuantity() + pandHistory.getAdditionalQuantity());
            double mainQuantity = 0.0;
            DecimalFormat df = new DecimalFormat("#.###");

            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = myDateObj.format(myFormatObj);
            mainQuantity = Double.valueOf(df.format(pand.get().getTotalQuantity() + pandHistory.getAdditionalQuantity()));
            pand.get().setTotalQuantity(mainQuantity);
            pandHistory.setAdditionalQuantityDate(formattedDate);
            pandHistory.setPandCode(pand.get().getPandCode());
            pandHistory.setTotalAfterAddition(mainQuantity);
            pandHistory.setPandId(pand.get().getId());
            pandHistory.setAdditionalBy(changeHistoryLog.getUser(request));
            List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.findByPandCodeAndProjectCode(pand.get().getPandCode(), pand.get().getProjectCode());
            double total = 0.0;

            for (int i = 0; i < pandsToJobOrders.size(); i++) {
                total += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());
                pandsToJobOrders.get(i).setQuantityInPand(mainQuantity - total);
                pandsToJobOrderRepository.save(pandsToJobOrders.get(i));
            }
            String formattedNumber = df.format(mainQuantity - total);
            pand.get().setRestQuantity(Double.valueOf(formattedNumber));
            pand.get().setTotalPrice(String.valueOf(mainQuantity*pand.get().getPrice()));

            return new ResponseEntity<>(pandHistoryRepository.save(pandHistory), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(pandHistory, HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<PandHistory> updatePandHistory(Long id, PandHistory updatedPandHistory) {
        try {
            Optional<PandHistory> pandHistory = pandHistoryRepository.findById(id);
            Optional<Pand> pand = pandsRepository.findById(pandHistory.get().getPandId());
            double mainQuantity = 0.0;
            DecimalFormat df = new DecimalFormat("#.###");

            if(pandHistory.get().getAdditionalQuantity() != updatedPandHistory.getAdditionalQuantity()) {
                double diffValue = pandHistory.get().getAdditionalQuantity() - updatedPandHistory.getAdditionalQuantity();
                if (pandHistory.get().getAdditionalQuantity() > updatedPandHistory.getAdditionalQuantity()) {
                    pand.get().setRestQuantity(pand.get().getRestQuantity() - diffValue);
                    pand.get().setMockQuantity(pand.get().getMockQuantity() - diffValue);
                    mainQuantity = Double.valueOf(df.format(pand.get().getTotalQuantity() - diffValue));
                    pand.get().setTotalQuantity(mainQuantity);
                } else {
                    if (diffValue < 0) {
                        diffValue *= -1;
                    }
                    pand.get().setRestQuantity(pand.get().getRestQuantity() + diffValue);
                    pand.get().setMockQuantity(pand.get().getMockQuantity() + diffValue);
                    mainQuantity = Double.valueOf(df.format(pand.get().getTotalQuantity() + diffValue));
                    pand.get().setTotalQuantity(mainQuantity);
                }
                pand.get().setTotalPrice(String.valueOf(mainQuantity * pand.get().getPrice()));

                pandsRepository.save(pand.get());
                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = myDateObj.format(myFormatObj);

                pandHistory.get().setAdditionalQuantityDate(formattedDate);
                pandHistory.get().setAdditionalQuantity(updatedPandHistory.getAdditionalQuantity());
                pandHistory.get().setAdditionalReason(updatedPandHistory.getAdditionalReason());
                pandHistory.get().setTotalAfterAddition(pand.get().getTotalQuantity());
                List<PandsToJobOrder> pandsToJobOrders = pandsToJobOrderRepository.findByPandCodeAndProjectCode(pand.get().getPandCode(), pand.get().getProjectCode());
                double total = 0.0;

                for (int i = 0; i < pandsToJobOrders.size(); i++) {
                    total += Double.valueOf(pandsToJobOrders.get(i).getMainTotal());
                    pandsToJobOrders.get(i).setQuantityInPand(mainQuantity - total);
                    pandsToJobOrderRepository.save(pandsToJobOrders.get(i));
                }
                pandHistoryRepository.save(pandHistory.get());
            }
            return new ResponseEntity<>(pandHistory.get(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(updatedPandHistory, HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ResponseEntity<PandHistory> deletePandHistory(Long id) {
        try {
            Optional<PandHistory> pandHistory = pandHistoryRepository.findById(id);

            Optional<Pand> pand = pandsRepository.findById(pandHistory.get().getPandId());
            pand.get().setRestQuantity(pand.get().getRestQuantity() + pandHistory.get().getAdditionalQuantity());
            pand.get().setMockQuantity(pand.get().getRestQuantity() + pandHistory.get().getAdditionalQuantity());
            pand.get().setTotalQuantity(pand.get().getTotalQuantity() + pandHistory.get().getAdditionalQuantity());
            pandsRepository.save(pand.get());

            pandHistoryRepository.delete(pandHistory.get());
            return new ResponseEntity<>(pandHistory.get(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

}
