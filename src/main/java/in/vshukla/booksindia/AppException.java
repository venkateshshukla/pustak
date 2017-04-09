package in.vshukla.booksindia;

/**
 * This runtime exception is thrown when an unresolvable scenario exists in the application.
 * Created by venkatesh on 9/4/17.
 */
public class AppException extends RuntimeException {

    public AppException(String s) {
        super(s);
    }

    public AppException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AppException(Throwable throwable) {
        super(throwable);
    }

    public AppException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
