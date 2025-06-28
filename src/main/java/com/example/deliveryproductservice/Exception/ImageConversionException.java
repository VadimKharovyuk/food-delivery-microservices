package com.example.deliveryproductservice.Exception;

public class ImageConversionException extends RuntimeException {
    public ImageConversionException(String message) {
        super(message);
    }

    public ImageConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
