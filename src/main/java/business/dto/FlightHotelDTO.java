package business.dto;

import java.util.List;

import business.external.flight.FlightDTO;
import business.external.room.RoomInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlightHotelDTO {
    private List<FlightDTO> listFlight;
    private List<RoomInfoDTO> listHotel;
}
