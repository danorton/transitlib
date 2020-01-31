import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 * © 2020 Daniel Norton
 */
public class VehiclePositionUpdate {
    private final String url;
    List<VehiclePosition> vehiclePositions;

    VehiclePositionUpdate(final String url) {
        this.url = url;
    }

    public void fetch() throws Exception {

        // fetch the raw data from the server
        CloseableHttpResponse response;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String stringBuffer;
        try {
            HttpGet httpGet = new HttpGet(this.url);
            response = httpclient.execute(httpGet);
            try {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode / 100 != 2) {
                    throw new IOException("Invalid response status code: " + statusCode);
                }
                // Decode the protocol buffer
                InputStream is = response.getEntity().getContent();
                stringBuffer = IOUtils.toString(is, StandardCharsets.US_ASCII.name());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}
