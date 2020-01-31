import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class VehiclePositionUpdateTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void fetch() {
        VehiclePositionUpdate vehiclePositionUpdate =
                new VehiclePositionUpdate("https://data.texas.gov/api/views/eiei-9rpf/files/bfc933cc-9472-4689-950a-f1cee82d7b13");
        try {
            vehiclePositionUpdate.fetch();
        } catch (Exception e) {
            if (e.getMessage() == null) {
                e = new Exception(e.getCause());
            }
            e.printStackTrace();
        }
    }
}