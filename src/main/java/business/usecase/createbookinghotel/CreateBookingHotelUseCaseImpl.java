package business.usecase.createbookinghotel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.createreservation.model.CustomerInfo;
import msa.commons.commands.hotelbooking.CreateHotelBookingCommand;
import msa.commons.commands.hotelroom.model.RoomInfo;
import msa.commons.controller.hotel.booking.CreateHotelBookingDTO;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.user.UserValidate;
import msa.commons.saga.SagaPhases;

@Stateless
public class CreateBookingHotelUseCaseImpl implements CreateBookingHotelUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;

    @Override
    public boolean createHotelBooking(CreateHotelBookingDTO dto) {
        final String sagaId = UUID.randomUUID().toString();
        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setName(dto.getCustomer().getName());
        customerInfo.setEmail(dto.getCustomer().getEmail());
        customerInfo.setDni(dto.getCustomer().getDni());
        customerInfo.setPhone(dto.getCustomer().getPhone());
        Set<Long> repeatIds = new HashSet<>();
        List<RoomInfo> roomsInfo = new ArrayList<>();
        for (Long roomInfo : dto.getRoomsIds()) {
            long id = 0;
            try {
                id = roomInfo;
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid room ID: " + roomInfo);
            }
            if (id <= 0 || repeatIds.contains(id))
                throw new RuntimeException("Room ID " + id + " is repeated in the request");
            repeatIds.add(id);
            RoomInfo room = new RoomInfo();
            room.setRoomId(id + "");
        }

        final byte transActive = 1;
        TravelDTO travelDTO = new TravelDTO();
        travelDTO.setActive(false);
        travelDTO.setId(-1);
        travelDTO.setSagaId(sagaId);
        travelDTO.setSagaPhases(SagaPhases.STARTED);
        travelDTO.setTransactionActive(transActive);
        long travelId = this.travelService.createTravel(travelDTO);

        EventData eventData = new EventData(sagaId,
                UserValidate.CREATE_RESERVATION_HOTEL,
                Arrays.asList(EventId.ROLLBACK_CREATE_HOTEL_BOOKING),
                CreateHotelBookingCommand.builder()
                        .idTravelAgency(travelId)
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .numberOfNights(dto.getNumberOfNights())
                        .withBreakfast(dto.getWithBreakfast())
                        .peopleNumber(dto.getPeopleNumber())
                        .roomsInfo(roomsInfo)
                        .customerInfo(customerInfo)
                        .bookingId(-1L)
                        .travelUserId(Long.parseLong(dto.getUserId()))
                        .build(),
                transActive);
        this.eventHandlerRegistry.getHandler(EventId.VALIDATE_USER).handleCommand(this.gson.toJson(eventData));
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
