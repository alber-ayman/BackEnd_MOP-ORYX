package com.example.demo.models;

import jakarta.annotation.Nullable;
import lombok.Data;


public interface BalanceReport {

    @Nullable
    Double getNumber();
    @Nullable
    Double getTotal();
}
