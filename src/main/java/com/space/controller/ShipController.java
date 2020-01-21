package com.space.controller;

import com.space.exceptions.BadRequestException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping(value = "/rest")
public class ShipController {

    private ShipService service;

    @Autowired
    public void setService(ShipService service) {
        this.service = service;
    }

    @RequestMapping(value = "/ships", method = RequestMethod.GET)
    public List<Ship> getShips(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "planet", required = false) String planet,
                        @RequestParam(value = "shipType", required = false) ShipType shipType,
                        @RequestParam(value = "after", required = false) Long after,
                        @RequestParam(value = "before", required = false) Long before,
                        @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                        @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                        @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                        @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                        @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                        @RequestParam(value = "minRating", required = false) Double minRating,
                        @RequestParam(value = "maxRating", required = false) Double maxRating,
                        @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                        @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

        return service.getShips(Specification.where(service.filterByName(name)
                .and(service.filterByPlanet(planet))
                .and(service.filterByShipType(shipType))
                .and(service.filterByProdDate(after, before))
                .and(service.filterByUsage(isUsed))
                .and(service.filterBySpeed(minSpeed, maxSpeed))
                .and(service.filterByCrewSize(minCrewSize, maxCrewSize))
                .and(service.filterByRating(minRating, maxRating))), pageable).getContent();
    }

    @RequestMapping(value = "/ships/count", method = RequestMethod.GET)
    public Integer getShipsCount(@RequestParam(value = "name", required = false) String name,
                                 @RequestParam(value = "planet", required = false) String planet,
                                 @RequestParam(value = "shipType", required = false) ShipType shipType,
                                 @RequestParam(value = "after", required = false) Long after,
                                 @RequestParam(value = "before", required = false) Long before,
                                 @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                 @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                 @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                 @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                 @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                 @RequestParam(value = "minRating", required = false) Double minRating,
                                 @RequestParam(value = "maxRating", required = false) Double maxRating) {

        return service.getShipsCount(Specification.where(service.filterByName(name)
                .and(service.filterByPlanet(planet))
                .and(service.filterByShipType(shipType))
                .and(service.filterByProdDate(after, before))
                .and(service.filterByUsage(isUsed))
                .and(service.filterBySpeed(minSpeed, maxSpeed))
                .and(service.filterByCrewSize(minCrewSize, maxCrewSize))
                .and(service.filterByRating(minRating, maxRating))));
    }

    @RequestMapping(value = "/ships", method = RequestMethod.POST)
    public @ResponseBody Ship createShip(@RequestBody Ship ship) {
        if (ship.getName() == null
                || ship.getPlanet() == null
                || ship.getShipType() == null
                || ship.getProdDate() == null
                || ship.getSpeed() == null
                || ship.getCrewSize() == null) throw new BadRequestException("Not enough data for creating a ship");
        if (ship.getUsed() == null) ship.setUsed(false);
        return service.createShip(checkShip(ship));
    }

    @RequestMapping(value = "/ships/{id}", method = RequestMethod.GET)
    public @ResponseBody Ship getShipById(@PathVariable(value = "id") String id) {
        return service.getShipById(parseId(id));
    }

    @RequestMapping(value = "/ships/{id}", method = RequestMethod.POST)
    public @ResponseBody Ship updateShip(@PathVariable(value = "id") String id, @RequestBody Ship ship) {
        return service.updateShip(parseId(id), checkShip(ship));
    }


    @DeleteMapping(value = "/ships/{id}")
    public void deleteShip(@PathVariable(value = "id") String id) {
        service.deleteById(parseId(id));
    }

    private Long parseId(String id) {
        Long num;
        try {
            num = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Id is not a number");
        }
        if (num <= 0) throw new BadRequestException("Id is less than one");
        return num;
    }

    private Ship checkShip(Ship ship) {

        String s = ship.getName();
        if (s != null && (s.length() < 1 || s.length() > 50)) throw new BadRequestException("Name length is not between 1 and 50");

        s = ship.getPlanet();
        if (s != null && (s.length() < 1 || s.length() > 50)) throw new BadRequestException("Planet name length is not between 1 and 50");

        if (ship.getProdDate() != null) {
            int year = ship.getProdDate().getYear() + 1900;
            if (year < 2800 || year > 3019) throw new BadRequestException("Production date is wrong");
        }

        Double speed = ship.getSpeed();
        if (speed != null && (speed < 0.01d || speed > 0.99d)) throw new BadRequestException("Speed is wrong");

        Integer crew = ship.getCrewSize();
        if (crew != null && (crew < 1 || crew > 9999)) throw new BadRequestException("Crew size is wrong");

        return ship;
    }
}
