package com.adonai.admissiontracker;

import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.URI;

public class HttpClient {
    private final String USER_AGENT_STRING = "Mozilla/5.0 (X11; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0";
    private final CookieManager manager;
    String currentURL = "";

    public HttpClient() {
        manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
    }

    public CookieStore getCookieStore() {
        return manager.getCookieStore();
    }

    public String getPageAsString(String url) {
        if (url.startsWith("file"))
            return null; // Не загружать локальные

        HttpURLConnection httpGet = null;
        try {
            final URI address = new URI(currentURL).resolve(url);
            httpGet = (HttpURLConnection) address.toURL().openConnection();
            setDefaultParameters(httpGet);

            return getResponseString(httpGet);
        } catch (Exception ignored) {
        } // stream close / timeout
        finally {
            if (httpGet != null)
                httpGet.disconnect();
        }

        return null;
    }

    public byte[] getPageAsByteArray(String url) {
        if (url.startsWith("file"))
            return null; // Не загружать локальные

        HttpURLConnection httpGet = null;
        try {
            final URI address = new URI(currentURL).resolve(url);
            httpGet = (HttpURLConnection) address.toURL().openConnection();
            setDefaultParameters(httpGet);
            // getting bytes of image
            final InputStream is = httpGet.getInputStream();
            final byte[] buffer = new byte[8192];
            int bytesRead;
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = is.read(buffer)) != -1)
                output.write(buffer, 0, bytesRead);
            is.close();
            httpGet.disconnect();

            return output.toByteArray();
        } catch (Exception ignored) {
        } // stream close / timeout
        finally {
            if (httpGet != null)
                httpGet.disconnect();
        }

        return null;
    }

    public String getPageAndContextAsString(String url) {
        HttpURLConnection httpGet = null;
        try {
            final URI address = new URI(currentURL).resolve(url.trim().replace(" ", "")); // убиваем символ Non-breaking space
            currentURL = address.toURL().toString();
            httpGet = (HttpURLConnection) address.toURL().openConnection();
            setDefaultParameters(httpGet);

            return getResponseString(httpGet);
        } catch (Exception ignored) {
        } // stream close / timeout
        finally {
            if (httpGet != null)
                httpGet.disconnect();
        }

        return null;
    }

    public String getResponseString(HttpURLConnection httpGet) throws IOException {
        final String contentType = httpGet.getContentType();
        final String[] values = contentType.split(";");
        String charset = "windows-1251";

        for (String value : values) {
            value = value.trim();

            if (value.toLowerCase().startsWith("charset=")) {
                charset = value.substring("charset=".length());
                break;
            }
        }

        return new String(getResponseBytes(httpGet), charset);
    }

    private byte[] getResponseBytes(HttpURLConnection httpGet) throws IOException {
        final InputStream is = httpGet.getInputStream();
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) >= 0)
            stream.write(buffer, 0, bytesRead);
        is.close();
        httpGet.disconnect();

        return stream.toByteArray();
    }

    /**
     * Manual page processing
     * Remember to disconnect connection!
     *
     * @param url url to fetch
     * @return connection for manual usage
     */
    public HttpURLConnection getPageAndContext(String url) {
        try {
            final URI address = new URI(currentURL).resolve(url.trim().replace(" ", "")); // убиваем символ Non-breaking space
            currentURL = address.toURL().toString();
            final HttpURLConnection httpGet = (HttpURLConnection) address.toURL().openConnection();
            setDefaultParameters(httpGet);
            return httpGet;
        } catch (Exception ignored) {
        }

        return null;
    }

    private void setDefaultParameters(HttpURLConnection conn) {
        conn.setRequestProperty(HTTP.USER_AGENT, USER_AGENT_STRING);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
    }
}
