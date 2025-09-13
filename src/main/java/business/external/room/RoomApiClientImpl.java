package business.external.room;

import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Stateless
public class RoomApiClientImpl implements RoomApiClient{
    private Client client = ClientBuilder.newClient();
    private static final String PATH = "http://localhost:9001/msa-hotel-room/api/room/";

    @Override
    public List<RoomInfoDTO> getRoomByParams(String country, String hotelName) {
        Response r = this.client.target(PATH + "params")
                .queryParam("country", country)
                .queryParam("hotelName", hotelName)
                .request()
                .get();
        if (r.getStatus() == 200)
            return r.readEntity(new GenericType<List<RoomInfoDTO>>() {});

        return Collections.emptyList();
    }
    
}
