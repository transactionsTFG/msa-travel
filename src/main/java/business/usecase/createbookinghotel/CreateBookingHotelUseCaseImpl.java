package business.usecase.createbookinghotel;

import java.util.Arrays;
import java.util.HashSet;
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
import msa.commons.event.eventoperation.reservation.CreateReservation;
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
        customerInfo.setDni(dto.getCustomerDNI());
        customerInfo.setName(dto.getCustomer().getName());
        customerInfo.setEmail(dto.getCustomer().getEmail());
        customerInfo.setDni(dto.getCustomer().getDni());
        Set<String> repeatIds = new HashSet<>();
        for (RoomInfo roomInfo : dto.getRoomsInfo()) {
            long id = 0;
            try {  
                id = Long.parseLong(roomInfo.getRoomId());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid room ID: " + roomInfo.getRoomId());
            }
            if (roomInfo.getRoomId() == null || id <= 0 || repeatIds.contains(roomInfo.getRoomId())) 
                throw new RuntimeException("Room ID " + roomInfo.getRoomId() + " is repeated in the request"); 
            repeatIds.add(roomInfo.getRoomId());
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
                UserValidate.CREATE_RESERVATION_AIRLINE,
                Arrays.asList(EventId.ROLLBACK_CREATE_HOTEL_BOOKING),
                CreateHotelBookingCommand.builder()
                        .sagaId(sagaId)
                        .idTravelAgency(travelId)
                        .userId(-1L)
                        .startDate(dto.getStartDate())
                        .endDate(dto.getEndDate())
                        .numberOfNights(dto.getNumberOfNights())
                        .withBreakfast(dto.getWithBreakfast())
                        .peopleNumber(dto.getPeopleNumber())
                        .customerDNI(dto.getCustomerDNI())
                        .roomsInfo(dto.getRoomsInfo())
                        .customerInfo(customerInfo)
                        .bookingId(dto.getBookingId())
                        .travelUserId(Long.parseLong(dto.getUserId()))
                        .build(),
                transActive);
        eventData.setOperation(UserValidate.CREATE_RESERVATION_HOTEL);
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
