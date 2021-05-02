package de.dm.collector.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Request {

    private final String                    filename;
    private final Hashtable<String, String> attributes;

    @SuppressWarnings("deprecation")
    public Request(String request) {
        try {
            request = URLDecoder.decode(request, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // try best guess
            request = URLDecoder.decode(request);
        }

        StringTokenizer st = new StringTokenizer(request, "?");

        filename = st.nextToken();
        attributes = new Hashtable<String, String>();

        if (st.hasMoreTokens()) {
            String attribs = st.nextToken();
            if (attribs != null) {
                st = new StringTokenizer(attribs, "&");
                while (st.hasMoreTokens()) {
                    String next = st.nextToken();
                    String[] split = next.split("=");
                    if (split.length > 1) {
                        attributes.put(split[0], split[1]);
                    } else {
                        attributes.put(next, "");
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getFilename());
        sb.append("?");

        LinkedList<String> keys = new LinkedList<String>();
        Enumeration<String> e = attributes.keys();
        if (e.hasMoreElements()) {
            for (String key = e.nextElement(); e.hasMoreElements(); key = e.nextElement()) {
                keys.add(key);
            }
            Collections.sort(keys);
            boolean first = true;
            for (String key : keys) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(key);
                String attr = attributes.get(key);
                if (attr != null) {
                    sb.append("=");
                    sb.append(attr);
                }
                first = false;
            }
        }
        return sb.toString();
    }

    public String getFilename() {
        return filename;
    }

    public Hashtable<String, String> getAttributes() {
        return attributes;
    }
}