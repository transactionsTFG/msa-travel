package business.service;

import business.travel.TravelDTO;

public interface TravelService {
    long createTravel(TravelDTO travelDTO);
    TravelDTO getTravelById(long id);
    TravelDTO updateTravel(TravelDTO travelDTO);
}
