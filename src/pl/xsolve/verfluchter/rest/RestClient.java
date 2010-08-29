/*
 * This file is part of verfluchter-android.
 *
 * verfluchter-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * verfluchter-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.xsolve.verfluchter.rest;

import android.util.Log;
import android.util.Pair;
import com.google.common.base.Charsets;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import pl.xsolve.verfluchter.tools.Constants;
import pl.xsolve.verfluchter.tools.SoulTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/
 * Created on April 27th, 2010
 *
 * @author by Luke Lowrey
 */
public class RestClient {

    // logger tag
    private final static String TAG = RestClient.class.getSimpleName();

    // headers and params to be passed in request
    private List<NameValuePair> paramz = new ArrayList<NameValuePair>();
    private List<NameValuePair> headerz = new ArrayList<NameValuePair>();

    // basic authentification if needed
    Pair<String, String> basicAuthCredentials = new Pair<String, String>("", "");

    // internal stuff
    private HttpResponse httpResponse;
    private DefaultHttpClient httpclient;
    private ClientConnectionManager clientConnectionManager;
    private HttpContext context;
    private SchemeRegistry schemeRegistry = new SchemeRegistry();
    private List<Cookie> cookies = new ArrayList<Cookie>();

    public RestClient() {
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
    }

    public RestResponse execute(String domain, RequestMethod method) throws IOException {
        RestResponse response = null;

        addHeader("Pragma", "no-cache");
        addHeader("Cache-Control", "no-cache");
        addHeader("User-Agent", Constants.USER_AGENT);

        switch (method) {
            case GET:
                Log.v(TAG, "Performing " + method + " on " + domain);
                response = doGET(domain);
                break;
            case POST:
                Log.v(TAG, "Performing " + method + " on " + domain);
                response = doPOST(domain);
                break;
        }

        return response;
    }

    private RestResponse doGET(String domain) throws IOException {
        String combinedParams = prepareGETCombinedParams();
        HttpGet request = new HttpGet(SoulTools.prefixHttpsIfNeeded(domain) + combinedParams);

        //add headerz
        for (NameValuePair h : headerz) {
            request.addHeader(h.getName(), h.getValue());
        }

        return executeRequest(request);
    }

    private String prepareGETCombinedParams() throws UnsupportedEncodingException {
        if (paramz.size() > 0) {
            StringBuilder combinedParams = new StringBuilder("?");
            for (NameValuePair p : paramz) {
                String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), Charsets.UTF_8.name());

                if (combinedParams.length() > 1) {
                    combinedParams.append("&").append(paramString);
                } else {
                    combinedParams.append(paramString);
                }
            }
            return combinedParams.toString();
        }
        return "";
    }

    private RestResponse doPOST(String domain) throws IOException {
        HttpPost request = new HttpPost(SoulTools.prefixHttpsIfNeeded(domain));

        //add headerz
        for (NameValuePair h : headerz) {
            request.addHeader(h.getName(), h.getValue());
        }

        if (!paramz.isEmpty()) {
            request.setEntity(new UrlEncodedFormEntity(paramz, Charsets.UTF_8.name()));
        }

        return executeRequest(request);
    }

    private RestResponse executeRequest(HttpUriRequest request) throws IOException {
        Log.v(TAG, "Final request preperations...");

        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 1);
        httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(1));
        httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, Charsets.UTF_8.name());

        context = new BasicHttpContext();

//        if (basicAuthCredentials != null) {
        // ignore that the ssl cert is self signed
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(null, AuthScope.ANY_PORT),// null here means "any host is OK"
                new UsernamePasswordCredentials(basicAuthCredentials.first, basicAuthCredentials.second));
        clientConnectionManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

        context.setAttribute("http.auth.credentials-provider", credentialsProvider);
//        }

        //connection (client has to be created for every new connection)
        httpclient = new DefaultHttpClient(clientConnectionManager, httpParams);

        for (Cookie cookie : cookies) {
            Log.v(TAG, "Using cookie " + cookie.getName() + "=" + cookie.getValue() + "...");
            httpclient.getCookieStore().addCookie(cookie);
        }

        try {
            httpResponse = httpclient.execute(request, context);

            int responseCode = httpResponse.getStatusLine().getStatusCode();
            Header[] headers = httpResponse.getAllHeaders();
            String errorMessage = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            Log.v(TAG, "Got cookies:");
            cookies = httpclient.getCookieStore().getCookies();
            if (cookies.isEmpty()) {
                Log.v(TAG, "None");
            } else {
                for (Cookie cookie : cookies) {
                    Log.v(TAG, "---- " + cookie.toString());
                }
            }

            String message = null;
            InputStream inStream = entity.getContent();
            message = SoulTools.convertStreamToString(inStream);

            // Closing the input stream will trigger connection release
            entity.consumeContent();
            inStream.close();

            return new RestResponse(responseCode, message, headers, cookies, errorMessage);
        } catch (ClientProtocolException e) {
            Log.v(TAG, "Encountered ClientProtocolException!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.v(TAG, "Encountered IOException!");
            e.printStackTrace();
        } finally {
            //always shutdown the connection manager
            httpclient.getConnectionManager().shutdown();
        }

        Log.v(TAG, "Returning null RestResponse!");
        return null;
    }


    public void addParam(String name, String value) {
        paramz.add(new BasicNameValuePair(name, value));
    }

    public void addHeader(String name, String value) {
        headerz.add(new BasicNameValuePair(name, value));
    }

    public void addCookies(Cookie... newCookies) {
        cookies = new ArrayList<Cookie>(newCookies.length);
        cookies.addAll(Arrays.asList(newCookies));
    }

    public void setupBasicAuth(String basicLogin, String basicPass) {
        basicAuthCredentials = new Pair<String, String>(basicLogin, basicPass);
    }
}