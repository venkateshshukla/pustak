package in.vshukla.booksindia.exceptions;

/**
 * This runtime exception is thrown when an unresolvable scenario exists in the application.
 * Created by venkatesh on 9/4/17.
 */
public class AppRuntimeException extends RuntimeException {

    public AppRuntimeException(String s) {
        super(s);
    }

    public AppRuntimeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AppRuntimeException(Throwable throwable) {
        super(throwable);
    }

}
