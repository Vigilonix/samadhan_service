package com.vigilonix.jaanch.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class OdReceiptPojo {
    private String reportUuid;
    private String applicationUuid;
    private Long createdOn;
    private String receiptId;
}
