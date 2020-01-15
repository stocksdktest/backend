package com.cvicse.leasing.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "ContractRequest Not Found")
public class DocumentNotFoundException extends Exception {

        private static final long serialVersionUID = 1L;

        public DocumentNotFoundException(String errorMessage) {
            super(errorMessage);
        }

        @Override
        public String getMessage() {
            return super.getMessage();
        }
}
