package com.weirdocomputing.transitlib;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * © 2020 Daniel Norton
 */
public class Route {
    @SuppressWarnings("FieldCanBeLocal")
    private final Logger logger = LoggerFactory.getLogger(Route.class);
    private static final boolean VERBOSE_WARNINGS = false;

    public final Color DEFAULT_COLOR = Color.WHITE;
    public final Color DEFAULT_TEXT_COLOR = Color.BLACK;

    private String id = null;
    private String agencyId = null;
    private String shortName = null;
    private String longName = null;
    private String desc = null;
    private RouteType type = null;
    private URL url = null;
    public Color color = DEFAULT_COLOR;
    public Color textColor = DEFAULT_TEXT_COLOR;
    private int sortOrder = 0;

    private static final JsonNodeFactory jnf = JsonNodeFactory.instance;

    public Route(AgencyCollection agencies, String[] keys, String[]  values) throws MalformedURLException {
        for (int i = 0; i < keys.length && i < values.length; i++) {
            String key = keys[i];
            String value = values[i];
            if (value != null) {
                value = value.strip();
                if (!value.isEmpty()) switch (key) {
                    case "route_id":
                        this.id = value;
                        break;
                    case "agency_id":
                        this.agencyId = value;
                        break;
                    case "route_short_name":
                        this.shortName = value;
                        break;
                    case "route_long_name":
                        this.longName = value;
                        break;
                    case "route_desc":
                        this.desc = value;
                        break;
                    case "route_type":
                        this.type = RouteType.fromLong(Integer.parseInt(value));
                        break;
                    case "route_url":
                        this.url = new URL(value);
                        break;
                    case "route_color":
                        if (value.charAt(0) == '+') {
                            throw new NumberFormatException("route_color is not valid");
                        }
                        this.color = new Color(Integer.parseUnsignedInt(value, 16));
                        if (this.color.equals(DEFAULT_COLOR)) {
                            this.color = null;
                        }
                        break;
                    case "route_text_color":
                        if (value.charAt(0) == '+') {
                            throw new NumberFormatException("route_text_color is not valid");
                        }
                        this.textColor = new Color(Integer.parseUnsignedInt(value, 16));
                        if (this.textColor.equals(DEFAULT_TEXT_COLOR)) {
                            this.textColor = null;
                        }
                        break;
                    case "route_sort_order":
                        this.sortOrder = Integer.parseUnsignedInt(value);
                    default:
                        logger.warn("Ignoring unrecognized column: \"{}\"", key);
                }
            }
        }

        // set defaults for missing or blank optional values
        if (this.agencyId == null && agencies.size() < 2) { // missing agency, but there's only one, anyway
            this.agencyId = agencies.getDefaultAgency().getId();
        }

        // validate
        if (this.id == null || this.id.isBlank()) {
            throw new IllegalArgumentException("route_id must not be blank");
        } else if (this.agencyId == null) {
            throw new IllegalArgumentException("agency_id must not be blank");
        } else if (agencies.get(this.agencyId) == null) {
            throw new IllegalArgumentException("agency_id must reference known agency");
        } else if (this.shortName == null && this.longName == null) {
            throw new IllegalArgumentException("route_short_name and route_long_name must not both be blank");
        } else if (this.type == null) {
            throw new IllegalArgumentException("route_type must not be blank");
        }

        // Warn about things that we allow, but that are problematic
        if (VERBOSE_WARNINGS) {
            if (this.url != null && !this.url.getProtocol().equals("https")) {
                logger.warn("route_url should specify an HTTPS protocol");
            }
        }
    }

    public ObjectNode toJsonObject() {
        ObjectNode o = jnf.objectNode();
        o.put("id", this.id);
        o.put("agencyId", this.agencyId);
        if (this.shortName != null) {
            o.put("shortName", this.shortName);
        }
        if (this.longName != null) {
            o.put("longName", this.longName);
        }
        if (this.desc != null) {
            o.put("desc", this.desc);
        }
        o.put("type", this.type.getIndex());
        if (this.url != null) {
            o.put("url", this.url.toString());
        }
        if (this.color != null) {
            o.put("color", String.format("%06X", this.color.getRGB() & 0xFFFFFF));
        }
        if (this.textColor != null) {
            o.put("textColor", String.format("%06X", this.textColor.getRGB() & 0xFFFFFF));
        }
        if (this.sortOrder > 0) {
            o.put("sortOrder", this.sortOrder);
        }
        return o;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getAgencyId() {
        return agencyId;
    }

    @Nullable
    public String getShortName() {
        return shortName;
    }

    @Nullable
    public String getLongName() {
        return longName;
    }

    @Nullable
    public String getDesc() {
        return desc;
    }

    @NotNull
    public RouteType getType() {
        return type;
    }

    @Nullable
    public URL getUrl() {
        return url;
    }

    @Nullable
    public Color getColor() {
        return color;
    }

    @Nullable
    public Color getTextColor() {
        return textColor;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
