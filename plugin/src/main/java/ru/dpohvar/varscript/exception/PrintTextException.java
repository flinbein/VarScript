package ru.dpohvar.varscript.exception;

/**
 * This exception this exception will be printed for player as raw text
 */
public class PrintTextException extends RuntimeException {
    public PrintTextException(String message) {
        super(message);
    }
}
