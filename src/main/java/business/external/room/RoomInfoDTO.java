package business.external.room;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomInfoDTO extends RoomDTO{
    private String countryName;
    private String hotelName;
    public RoomInfoDTO() {
    }
    public RoomInfoDTO(long id, long hotelId, int number, boolean singleBed, boolean available, int peopleNumber, double dailyPrice, String country, String hotelName) {
        super(id, hotelId, number, singleBed, available, peopleNumber, dailyPrice);
        this.countryName = country;
        this.hotelName = hotelName;
    }
}

