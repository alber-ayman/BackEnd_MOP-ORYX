package com.example.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkOrderStatus {
    UNDER_FABRICATION(1),
    DELIVERED(2),
    CLOSED(3);

    private int value;
    WorkOrderStatus(int value) {}

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static WorkOrderStatus fromValue(int value) {
        return WorkOrderStatus.values()[value];
    }
}
