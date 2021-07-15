package org.opendatakit.suitcase.model;

public class SyncClientError {
    private static final int UNKNOWN_ERROR_STATUS_CODE = 500;
    private static final String UNKNOWN_ERROR_MESSAGE = "Unknown error occurred";
    private final int statusCode;
    private final String message;

    public SyncClientError(Exception e) {
        String errorMsg = e.getCause().getMessage();
        int startIndex = errorMsg.indexOf("Status");
        if (startIndex != -1) {
            /*
            Error message in HTML response is of format "HTTP Status XXX - Error Message" .
            As status code is 13 indexes starting from the start of "Status" we add add 13 to start and take next 3 elements.
             */
            statusCode = Integer.parseInt(errorMsg.substring(startIndex + 7, startIndex + 10));
            message = errorMsg.substring(startIndex + 13);
        } else {
            statusCode = UNKNOWN_ERROR_STATUS_CODE;
            startIndex = errorMsg.indexOf("<message>");
            int endIndex = errorMsg.indexOf("</message>");
             /*
            Error message in HTML response is sometimes inside the <message> tag.
            If Status code is not found we implement this login to parse the error.
             */
            if(startIndex !=-1) {
                message = errorMsg.substring(startIndex+9,endIndex);
            }
            else {
                message = UNKNOWN_ERROR_MESSAGE;
            }

        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

}