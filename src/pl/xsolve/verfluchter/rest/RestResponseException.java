package pl.xsolve.verfluchter.rest;

/**
 * @author Konrad Ktoso Malawski
 */
public class RestResponseException extends Exception {

    static final long serialVersionUID = 1;

    public RestResponseException() {
        super();
    }

    public RestResponseException(String message) {
        super(message);
    }

    public RestResponseException(int errorCode) {
        super("The server returned an " + errorCode + " error code.");
    }
}
