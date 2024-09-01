package com.vigilonix.jaanch.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhatsappFileUploadResponse {
        private int status;
        private boolean flag;
        private WhatsappDataResponse data;
        private String message;
}
