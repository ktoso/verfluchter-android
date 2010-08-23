package pl.xsolve.verfluchter.tools;

/**
 * @author Konrad Ktoso Malawski
 */
public interface PasswdUtil {
    String encrypt(String password);

    String decrypt(String password);
}
