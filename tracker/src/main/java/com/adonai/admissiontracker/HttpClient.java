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
import java.net.URL;

public class HttpClient {
    private final String USER_AGENT_STRING = "Mozilla/5.0 (X11; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0";
    private final CookieManager manager;

    private URL currentURL = null;
    private long lastModified = 0;

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
            final URL address = new URL(currentURL, url);
            httpGet = (HttpURLConnection) address.openConnection();
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

    public String getPageAndContextAsString(String url) throws IOException {
        HttpURLConnection httpGet = null;
        try {
            currentURL = new URL(currentURL, url);
            httpGet = (HttpURLConnection) currentURL.openConnection();
            setDefaultParameters(httpGet);

            return getResponseString(httpGet);
        } finally {
            if (httpGet != null)
                httpGet.disconnect();
        }
    }

    public String getResponseString(HttpURLConnection httpGet) throws IOException {
        String charset = "windows-1251";
        final String contentType = httpGet.getContentType();
        if(contentType != null) {
            final String[] values = contentType.split(";");
            for (String value : values) {
                value = value.trim();

                if (value.toLowerCase().startsWith("charset=")) {
                    charset = value.substring("charset=".length());
                    break;
                }
            }
        }

        long lastMod = httpGet.getLastModified();
        if(lastMod > 0)
            lastModified = lastMod;

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

    private void setDefaultParameters(HttpURLConnection conn) {
        conn.setRequestProperty(HTTP.USER_AGENT, USER_AGENT_STRING);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
    }

    public String getCurrentURL() {
        return currentURL.toString();
    }

    public long getLastModified() {
        return lastModified;
    }
}
