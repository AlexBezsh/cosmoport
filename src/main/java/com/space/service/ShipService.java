package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

public interface ShipService {
    Ship createShip(Ship ship);
    void deleteById(Long id);
    Ship updateShip(Long id, Ship ship);
    Ship getShipById(Long id);
    Integer getShipsCount(Specification<Ship> specification);
    Page<Ship> getShips(Specification<Ship> specification, Pageable pageable);
    Double calculateRating(Ship ship);

    Specification<Ship> filterByName(String name);
    Specification<Ship> filterByPlanet(String planet);
    Specification<Ship> filterByShipType(ShipType shipType);
    Specification<Ship> filterByProdDate(Long after, Long before);
    Specification<Ship> filterByUsage(Boolean isUsed);
    Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed);
    Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize);
    Specification<Ship> filterByRating(Double minRating, Double maxRating);
}
