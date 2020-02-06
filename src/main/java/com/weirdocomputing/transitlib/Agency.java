package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.validator.routines.EmailValidator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

/**
 *  © 2020 Daniel Norton
 */
public class Agency {
    private static final boolean VERBOSE_WARNINGS = false;
    private String id = null;
    private String name = null;
    private URL url = null;
    private TimeZone tz = null;
    private Locale locale = null;
    private String phone = null;
    private URL fareUrl = null;
    private String email = null;

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public Agency(String[] keys, String[]  values) throws IllegalStateException, MalformedURLException {
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
                        this.tz = TimeZone.getTimeZone(value);
                        break;
                    case "agency_lang":
                        if (value.equalsIgnoreCase("en")) {
                            this.locale = null; // use default if under-specified
                        } else {
                            this.locale = Locale.forLanguageTag(value);
                        }
                        break;
                    default:
                        System.err.printf("WARNING: Ignoring unrecognized column: \"%s\"\n", key);
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
        } else if (tz == null) {
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
                System.err.print("WARNING: agency_url should specify an HTTPS protocol\n");
            }
            if (this.fareUrl != null && !this.fareUrl.getProtocol().equals("https")) {
                System.err.print("WARNING: agency_fare_url should specify an HTTPS protocol\n");
            }
        }

    }

    public ObjectNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        o.put("id", this.id);
        o.put("name", this.name);
        o.put("url", this.url.toString());
        o.put("tz", this.tz.toZoneId().toString());
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

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public TimeZone getTz() {
        return tz;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getPhone() {
        return phone;
    }

    public URL getFareUrl() {
        return fareUrl;
    }

    public String getEmail() {
        return email;
    }
}