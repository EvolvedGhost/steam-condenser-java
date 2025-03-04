/*
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2010-2020, Sebastian Staudt
 */

package com.github.koraktor.steamcondenser.community;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.koraktor.steamcondenser.exceptions.WebApiException;

/**
 * This abstract class provides functionality for accessing Steam's Web API
 * <p>
 * The Web API requires you to register a domain with your Steam account to
 * acquire an API key. See http://steamcommunity.com/dev for further details.
 *
 * @author Sebastian Staudt
 */
abstract public class WebApi {

    protected static final Logger LOG = LoggerFactory.getLogger(WebApi.class);

    protected static String apiKey;

    protected static boolean secure = true;

    /**
     * Returns the Steam Web API key currently used by Steam Condenser
     *
     * @return The currently active Steam Web API key
     */
    public static String getApiKey() {
        return apiKey;
    }

    /**
     * Returns a raw list of interfaces and their methods that are available in
     * Steam's Web API
     *
     * This can be used for reference when accessing interfaces and methods
     * that have not yet been implemented by Steam Condenser.
     *
     * @return array The list of interfaces and methods
     */
    public static JSONArray getInterfaces()
            throws JSONException, WebApiException {
        String data = WebApi.getJSON("ISteamWebAPIUtil", "GetSupportedAPIList");
        return new JSONObject(data).
                getJSONObject("apilist").
                getJSONArray("interfaces");
    }

    /**
     * Sets the Steam Web API key
     *
     * @param apiKey The 128bit API key as a hexadecimal string that has to be
     *        requested from http://steamcommunity.com/dev
     * @throws WebApiException if the given API key is not a valid 128bit
     *        hexadecimal string
     */
    public static void setApiKey(String apiKey) throws WebApiException {
        if(apiKey != null && !apiKey.matches("^[0-9A-F]{32}$")) {
            throw new WebApiException(WebApiException.Cause.INVALID_KEY);
        }

        WebApi.apiKey = apiKey;
    }

    /**
     * Sets whether HTTPS should be used for the communication with the Web API
     *
     * @param secure Whether to use HTTPS
     */
    public static void setSecure(boolean secure) {
        WebApi.secure = secure;
    }

    /**
     * Fetches JSON data from Steam Web API using the specified interface,
     * method and version. Additional parameters are supplied via HTTP GET.
     * Data is returned as a JSON-encoded string.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @return Data is returned as a JSON-encoded string.
     * @throws WebApiException In case of any request failure
     */
    public static String getJSON(String apiInterface, String method)
            throws WebApiException {
        return load("json", apiInterface, method, 1, null);
    }

    /**
     * Fetches JSON data from Steam Web API using the specified interface,
     * method and version. Additional parameters are supplied via HTTP GET.
     * Data is returned as a JSON-encoded string.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @param version The API method version to use
     * @return Data is returned as a JSON-encoded string.
     * @throws WebApiException In case of any request failure
     */
    public static String getJSON(String apiInterface, String method, int version)
            throws WebApiException {
        return load("json", apiInterface, method, version, null);
    }

    /**
     * Fetches JSON data from Steam Web API using the specified interface,
     * method and version. Additional parameters are supplied via HTTP GET.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @param params Additional parameters to supply via HTTP GET
     * @param version The API method version to use
     * @return Data is returned as a JSON-encoded string.
     * @throws WebApiException In case of any request failure
     */
    public static String getJSON(String apiInterface, String method, int version, Map<String, Object> params)
            throws WebApiException {
        return load("json", apiInterface, method, version, params);
    }

    /**
     * Fetches JSON data from Steam Web API using the specified interface,
     * method and version. Additional parameters are supplied via HTTP GET.
     * Data is returned as a Hash containing the JSON data.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @return Data is returned as a <code>JSONObject</code>
     * @throws JSONException In case of misformatted JSON data
     * @throws WebApiException In case of any request failure
     */
    public static JSONObject getJSONData(String apiInterface, String method)
            throws JSONException, WebApiException {
        return getJSONData(apiInterface, method, 1, null);
    }

    /**
     * Fetches JSON data from Steam Web API using the specified interface,
     * method and version. Additional parameters are supplied via HTTP GET.
     * Data is returned as a Hash containing the JSON data.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @param version The API method version to use
     * @return Data is returned as a <code>JSONObject</code>
     * @throws JSONException In case of misformatted JSON data
     * @throws WebApiException In case of any request failure
     */
    public static JSONObject getJSONData(String apiInterface, String method, int version)
            throws JSONException, WebApiException {
        return getJSONData(apiInterface, method, version, null);
    }

    /**
     * Fetches JSON data from Steam Web API using the specified interface,
     * method and version. Additional parameters are supplied via HTTP GET.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @param params Additional parameters to supply via HTTP GET
     * @param version The API method version to use
     * @return Data is returned as a <code>JSONObject</code>
     * @throws JSONException In case of misformatted JSON data
     * @throws WebApiException In case of any request failure
     */
    public static JSONObject getJSONData(String apiInterface, String method, int version, Map<String, Object> params)
            throws JSONException, WebApiException {
        String data = getJSON(apiInterface, method, version, params);
        JSONObject result = new JSONObject(data).getJSONObject("result");

        if(result.getInt("status") != 1) {
            throw new WebApiException(WebApiException.Cause.STATUS_BAD, result.getInt("status"), result.getString("statusDetail"));
        }

        return result;
    }

    /**
     * Fetches data from Steam Web API using the specified interface, method
     * and version. Additional parameters are supplied via HTTP GET. Data is
     * returned as a String in the given format.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param format The format to load from the API ('json', 'vdf', or 'xml')
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @return Data is returned as a String in the given format (which may be
     *        "json", "vdf" or "xml").
     * @throws WebApiException In case of any request failure
     */
    public static String load(String format, String apiInterface, String method)
            throws WebApiException {
        return load(format, apiInterface, method, 1, null);
    }

    /**
     * Fetches data from Steam Web API using the specified interface, method
     * and version. Additional parameters are supplied via HTTP GET. Data is
     * returned as a String in the given format.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param format The format to load from the API ('json', 'vdf', or 'xml')
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @param version The API method version to use
     * @return Data is returned as a String in the given format (which may be
     *        "json", "vdf" or "xml").
     * @throws WebApiException In case of any request failure
     */
    public static String load(String format, String apiInterface, String method, int version)
            throws WebApiException {
        return load(format, apiInterface, method, version, null);
    }

    /**
     * Fetches data from Steam Web API using the specified interface, method
     * and version. Additional parameters are supplied via HTTP GET. Data is
     * returned as a String in the given format.
     *
     * @param apiInterface The Web API interface to call, e.g.
     *                     <code>ISteamUser</code>
     * @param format The format to load from the API ("json", "vdf", or "xml")
     * @param method The Web API method to call, e.g.
     *               <code>GetPlayerSummaries</code>
     * @param params Additional parameters to supply via HTTP GET
     * @param version The API method version to use
     * @return Data is returned as a String in the given format (which may be
     *        "json", "vdf", or "xml").
     * @throws WebApiException In case of any request failure
     */
    public static String load(String format, String apiInterface, String method, int version, Map<String, Object> params)
            throws WebApiException {
        String protocol = secure ? "https" : "http";
        String url = String.format("%s://api.steampowered.com/%s/%s/v%04d/?", protocol, apiInterface, method, version);

        if(params == null) {
            params = new HashMap<>();
        }
        params.put("format", format);
        if (apiKey != null) {
            params.put("key", apiKey);
        }

        boolean first = true;
        for(Map.Entry<String, Object> param : params.entrySet()) {
            if(first) {
                first = false;
            } else {
                url += '&';
            }

            url += String.format("%s=%s", param.getKey(), param.getValue());
        }

        if (LOG.isInfoEnabled()) {
            String debugUrl = (apiKey == null) ?
                url : url.replace(apiKey, "SECRET");
            LOG.info("Querying Steam Web API: " + debugUrl);
        }

        String data;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            ClassicHttpResponse response = httpClient.execute(request);

            int statusCode = response.getCode();
            if (!String.valueOf(statusCode).startsWith("20")) {
                if (statusCode == 401) {
                    throw new WebApiException(WebApiException.Cause.UNAUTHORIZED);
                }

                throw new WebApiException(WebApiException.Cause.HTTP_ERROR, statusCode, response.getReasonPhrase());
            }

            data = EntityUtils.toString(response.getEntity());
        } catch (WebApiException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApiException("Could not communicate with the Web API.", e);
        }


        return data;
    }

}
