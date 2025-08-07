package business.external.room;

import java.util.List;

public interface RoomApiClient {
    List<RoomInfoDTO> getRoomByParams(String country, String hotelName);
}
