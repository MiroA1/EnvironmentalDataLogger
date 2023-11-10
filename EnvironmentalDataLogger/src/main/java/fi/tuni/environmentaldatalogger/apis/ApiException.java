package fi.tuni.environmentaldatalogger.apis;

import java.io.IOException;

/**
 * Class for different API exceptions. WIP.
 */
public class ApiException extends IOException {

    /**
     * Enum for different error codes
     */
    public enum ErrorCode {
        CONNECTION_ERROR,
        INVALID_RESPONSE,
        PARSE_ERROR
    }
    private final ErrorCode errorCode;

    /**
     * Constructor
     * @param message The error message from API
     * @param errorCode The code for the error
     */
    public ApiException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Error code getter
     * @return error code as String
     */
    public String getErrorCode(){
        return this.errorCode.toString();
    }


}