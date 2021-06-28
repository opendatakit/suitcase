package org.opendatakit.suitcase.model;

public class ScanJsonException extends Exception{
    public ScanJsonException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ScanJsonException(Throwable cause) {
        super(cause);
    }

    public ScanJsonException(String message){ super(message);}
}
