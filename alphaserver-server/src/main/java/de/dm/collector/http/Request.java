package de.dm.collector.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class Request {

    private final String filename;
    private final Map<String, String> attributes;

    public Request(String request) {
        try {
            request = URLDecoder.decode(request, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // try best guess
            request = URLDecoder.decode(request, StandardCharsets.UTF_8);
        }

        StringTokenizer st = new StringTokenizer(request, "?");

        filename = st.nextToken();
        attributes = new Hashtable<>();

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

        if (!attributes.isEmpty()) {
            List<String> keys = attributes.keySet().stream().sorted().collect(Collectors.toList());
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

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
