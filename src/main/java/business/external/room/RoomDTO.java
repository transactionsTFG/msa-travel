package business.external.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {

    private long id;

    private long hotelId;

    private int number;

    private boolean singleBed;

    private boolean available;

    private int peopleNumber;

    private double dailyPrice;

}