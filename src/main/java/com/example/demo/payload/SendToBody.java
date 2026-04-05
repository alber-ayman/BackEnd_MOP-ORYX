package com.example.demo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendToBody {

    private boolean generalManager;

    private boolean manufacturingManager;

    private boolean storeManager;

    private boolean purchasingManager;

    private String note;
}
