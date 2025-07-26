package business.usecase.updatebooking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.dto.UpdateHotelBookingDTO;
import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.hotelbooking.UpdateHotelBookingCommand;
import msa.commons.commands.hotelroom.model.RoomInfo;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.UpdateReservation;
import msa.commons.saga.SagaPhases;

@Stateless
public class UpdateBookingUseCaseImpl implements IUpdateBookingUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    @Override
    public boolean updateBooking(UpdateHotelBookingDTO up) {
        TravelDTO travel = travelService.getTravelById(up.getTravelId());
        if (travel == null || !travel.isActive() || travel.getSagaPhases().equals(SagaPhases.STARTED) || travelService.getTravelByIdsExternal(0, up.getBookingId()) == null)
            throw new RuntimeException("Travel with ID " + up.getTravelId() + " not found");
        Set<Long> repeatIds = new HashSet<>();
        List<RoomInfo> roomsInfo = new ArrayList<>();
        for (Long roomInfo : up.getRoomsInfo()) {
            if (roomInfo == null || roomInfo <= 0 || repeatIds.contains(roomInfo)) 
                throw new RuntimeException("Room ID " + roomInfo + " is repeated in the request"); 
            repeatIds.add(roomInfo);
            RoomInfo i = new RoomInfo();
            i.setRoomId(String.valueOf(roomInfo));
            i.setDailyPrice((int)(Math.random() * 101));
            roomsInfo.add(i);
        }

        final String sagaId = UUID.randomUUID().toString();
        final byte transActive = 1;
        EventData eventData = new EventData(
            sagaId,
            UpdateReservation.UPDATE_RESERVATION_ONLY_HOTEL_BEGIN,
            new ArrayList<>(),
            UpdateHotelBookingCommand.builder()
            .customerDNI(up.getCustomerDNI())
            .bookingId(up.getBookingId())
            .startDate(up.getStartDate())
            .endDate(up.getEndDate())
            .numberOfNights(up.getNumberOfNights())
            .withBreakfast(up.getWithBreakfast())
            .peopleNumber(up.getPeopleNumber())
            .roomsInfo(roomsInfo)
            .idTravel(up.getTravelId())
            .build(),
            transActive
        );
        travel.setSagaId(sagaId);
        travel.setSagaPhases(SagaPhases.STARTED);
        travel.setTransactionActive(transActive);
        travelService.initTransaction(travel);
        this.eventHandlerRegistry.getHandler(EventId.UPDATE_RESERVATION_TRAVEL).handleCommand(this.gson.toJson(eventData));
        return true;
    }

    @Inject
    public void setTravelService(TravelService travelService) {
        this.travelService = travelService;
    }

    @Inject
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Inject
    public void setEventHandlerRegistry(EventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }
    
}
