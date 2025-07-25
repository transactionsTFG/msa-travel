package business.usecase.createtravelreservationbooking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import business.travel.TravelDTO;
import domainevent.registry.EventHandlerRegistry;
import msa.commons.commands.createreservation.CreateReservationCommand;
import msa.commons.commands.createreservation.model.CustomerInfo;
import msa.commons.commands.createreservation.model.IdFlightInstanceInfo;
import msa.commons.commands.hotelbooking.CreateHotelBookingCommand;
import msa.commons.commands.hotelroom.model.RoomInfo;
import msa.commons.controller.agency.reservationbooking.CreateAirlineAndHotelReservationDTO;
import msa.commons.controller.agency.reservationbooking.CreateAirlineAndHotelReservationInfoDTO;
import msa.commons.controller.airline.reservation.create.FlightInstanceSeatsDTO;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.user.UserValidate;
import msa.commons.saga.SagaPhases;

@Stateless
public class CreateTravelReservationBookingUseCase implements ICreateTravelReservationBookingUseCase {
    private EventHandlerRegistry eventHandlerRegistry;
    private TravelService travelService;
    private Gson gson;
    @Override
    public boolean createTravelReservationBooking(CreateAirlineAndHotelReservationDTO reservation) {
        CreateAirlineAndHotelReservationInfoDTO info = reservation.getInfoReservation();
        if (info == null)
            throw new RuntimeException("Info reservation is null");

        CustomerInfo customerInfo = new CustomerInfo();
        customerInfo.setDni(reservation.getDni());
        List<IdFlightInstanceInfo> listFlights = new ArrayList<>();
        for (FlightInstanceSeatsDTO f : info.getFlightInstances()) {
            if(f.getIdFlightInstance() <= 0 || f.getNumberSeats() <= 0)
                throw new RuntimeException("Invalid flight instance data: " + f);

            IdFlightInstanceInfo idFlightInstanceInfo = new IdFlightInstanceInfo();
            idFlightInstanceInfo.setIdFlightInstance(f.getIdFlightInstance());
            idFlightInstanceInfo.setNumberSeats(f.getNumberSeats());
            listFlights.add(idFlightInstanceInfo);
        }

        List<RoomInfo> roomsInfo = new ArrayList<>();
        for (Long roomId : info.getRoomIds()) {
            if (roomId == null || roomId <= 0)
                throw new RuntimeException("Room ID is null or empty");
            
            RoomInfo roomInfo = new RoomInfo();
            roomInfo.setRoomId(String.valueOf(roomId));            
            roomsInfo.add(roomInfo);    
        }

        final String sagaId = UUID.randomUUID().toString();
        final byte transactionActive = 2;
        TravelDTO travelDTO = new TravelDTO();
            travelDTO.setActive(false);
            travelDTO.setId(-1);
            travelDTO.setSagaId(sagaId);
            travelDTO.setSagaPhases(SagaPhases.STARTED);
            travelDTO.setTransactionActive(transactionActive);

        long travelId = this.travelService.createTravel(travelDTO);
        CreateReservationCommand createReservationCommand = CreateReservationCommand.builder()
                .idTravelAgency(travelId)
                .flightInstanceInfo(listFlights)
                .allFlightBuy(false)
                .customerInfo(customerInfo)
                .idUser(reservation.getIdUser())
                .build();

          CreateHotelBookingCommand createHotelBookingCommand = CreateHotelBookingCommand.builder()
                .sagaId(sagaId)
                .userId(-1L)
                .startDate(info.getStartDate())
                .endDate(info.getEndDate())
                .numberOfNights(info.getNumberOfNights())
                .withBreakfast(info.getWithBreakfast())
                .peopleNumber(info.getPeopleNumber())
                .customerDNI(reservation.getDni())
                .roomsInfo(roomsInfo)
                .customerInfo(customerInfo)
                .travelUserId(reservation.getIdUser())
                .idTravelAgency(travelId)
                .build();
        
        EventData eventDataReservationCommand = new EventData(sagaId, new ArrayList<>(), createReservationCommand);
        eventDataReservationCommand.setOperation(UserValidate.CREATE_RESERVATION_AIRLINE);
        eventDataReservationCommand.setTransactionActive(transactionActive);
        EventData eventDataHotelCommand = new EventData(sagaId, new ArrayList<>(), createHotelBookingCommand);
        eventDataHotelCommand.setOperation(UserValidate.CREATE_RESERVATION_HOTEL);
        eventDataHotelCommand.setTransactionActive(transactionActive);
        this.eventHandlerRegistry.getHandler(EventId.VALIDATE_USER).handleCommand(this.gson.toJson(eventDataReservationCommand));
        this.eventHandlerRegistry.getHandler(EventId.VALIDATE_USER).handleCommand(this.gson.toJson(eventDataHotelCommand));
        return false;
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
