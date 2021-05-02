/*
 * Created on 21.01.2005
 */
package de.dm.collector.http;

import java.io.IOException;

/**
 * @author Dennis Mueller
 * @date 21.01.2005
 */
public interface IHttpDataProvider {
    byte[] sendData(Request name) throws IOException;
}
