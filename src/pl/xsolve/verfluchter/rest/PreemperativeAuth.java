package pl.xsolve.verfluchter.rest;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * @author Konrad Ktoso Malawski
 */
class PreemptiveAuthRequestInterceptor implements HttpRequestInterceptor {

//    private String USER = getSetting(BASIC_AUTH_USER_S);
//    private String PASS = getSetting(BASIC_AUTH_PASS_S);

    PreemptiveAuthRequestInterceptor() {
    }

    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {

        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

        // If no auth scheme avaialble yet, try to initialize it preemptively
        if (authState.getAuthScheme() == null) {
            AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
            HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            if (authScheme != null) {
                Credentials creds = new UsernamePasswordCredentials(""/*USER*/, ""/*PASS*/);
                authState.setAuthScheme(authScheme);
                authState.setCredentials(creds);
            }
        }

    }

}
