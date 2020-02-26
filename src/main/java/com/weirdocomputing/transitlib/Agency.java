package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

/*
 *  © 2020 Daniel Norton
 */
/**
 *  Transit Agency; corresponds to a line from GTFS static feed agency.txt
 */
public class Agency {
    private static final Logger logger = LoggerFactory.getLogger(Agency.class);
    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;
    private static final boolean VERBOSE_WARNINGS = false;

    /**
     * Fields are defined by Google GTFS static feed file agency.txt
     */

    private String id = null;
    private String name = null;
    private URL url = null;
    private TimeZone timezone = null;
    private Locale locale = null;
    private String phone = null;
    private URL fareUrl = null;
    private String email = null;


    /**
     * Constructor from a row of values from agency.txt
     * @param keys First row of agency.txt
     * @param values current row of agency.txt
     * @throws MalformedURLException if a given URL is not valid
     */
    public Agency(String[] keys, String[]  values) throws MalformedURLException {
        for (int i = 0; i < keys.length && i < values.length; i++) {
            String key = keys[i];
            String value = values[i];
            if (value != null) {
                value = value.strip();
                if (!value.isEmpty()) switch (key) {
                    case "agency_id":
                        this.id = value;
                        break;
                    case "agency_name":
                        this.name = value;
                        break;
                    case "agency_phone":
                        this.phone = value;
                        break;
                    case "agency_email":
                        this.email = value;
                        break;
                    case "agency_url":
                        this.url = new URL(value);
                        break;
                    case "agency_fare_url":
                        this.fareUrl = new URL(value);
                        break;
                    case "agency_timezone":
                        this.timezone = TimeZone.getTimeZone(value);
                        break;
                    case "agency_lang":
                        if (value.equalsIgnoreCase("en")) {
                            this.locale = null; // use default if under-specified
                        } else {
                            this.locale = Locale.forLanguageTag(value);
                        }
                        break;
                    default:
                        logger.warn("Ignoring unrecognized column: \"{}\"", key);
                }
            }
        }

        // set defaults for missing optional values
        if (locale == null) {
            locale = Locale.US;
        }

        // validate
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("agency_id cannot be blank");
        } else if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("agency_name cannot be blank");
        } else if (url == null || url.toString().isBlank()) {
            throw new IllegalArgumentException("agency_url cannot be blank");
        } else if (timezone == null) {
            throw new IllegalArgumentException("agency_timezone cannot be blank");
        } else if (
                email != null &&
                !EmailValidator.getInstance().isValid(
                    email.trim().replaceFirst(  // replace TLD with ".com" to allow any TLD
                        "\\.\\p{Alpha}[\\p{Alnum}-]*\\p{Alnum}$", ".com"))) {
            throw new IllegalArgumentException("agency_email must specify a valid email address");
        }

        // Warn about things that we allow, but are problematic
        if (VERBOSE_WARNINGS) {
            if (!this.url.getProtocol().equals("https")) {
                logger.warn("agency_url should specify an HTTPS protocol");
            }
            if (this.fareUrl != null && !this.fareUrl.getProtocol().equals("https")) {
                logger.warn("agency_fare_url should specify an HTTPS protocol");
            }
        }

    }

    /**
     * Serialize to JSON object
     * @return JSON object
     */
    public JsonNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        o.put("id", this.id);
        o.put("name", this.name);
        o.put("url", this.url.toString());
        o.put("timezone", this.timezone.toZoneId().toString());
        o.put("lang", this.locale.toLanguageTag());
        if (this.phone != null) {
            o.put("phone", this.phone);
        }
        if (this.fareUrl != null) {
            o.put("fare_url", this.fareUrl.toString());
        }
        if (this.email != null) {
            o.put("email", this.email);
        }
        return o;
    }

    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    @SuppressWarnings("unused")
    public TimeZone getTimezone() {
        return timezone;
    }

    @SuppressWarnings("unused")
    public Locale getLocale() {
        return locale;
    }

    @SuppressWarnings("unused")
    public String getPhone() {
        return phone;
    }

    @SuppressWarnings("unused")
    public URL getFareUrl() {
        return fareUrl;
    }

    @SuppressWarnings("unused")
    public String getEmail() {
        return email;
    }
}
