package pl.xsolve.verfluchter.rest;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * @author Konrad Ktoso Malawski
 */
public class RestResponse {
    int responseCode;
    String response;
    String errorMessage;
    Header[] headers;
    List<Cookie> cookies;

    public RestResponse(int responseCode, String response, Header[] headers, List<Cookie> cookies, String errorMessage) {
        this.responseCode = responseCode;
        this.response = response;
        this.headers = headers;
        this.cookies = cookies;
        this.errorMessage = errorMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Header[] getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public Cookie getCookie(String cookieName) {
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(cookieName)){
                return cookie;
            }
        }
        return null;
    }
}
