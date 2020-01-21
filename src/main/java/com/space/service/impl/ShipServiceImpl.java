package com.space.service.impl;

import com.space.exceptions.ShipNotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class ShipServiceImpl implements ShipService {

    private ShipRepository repository;

    @Autowired
    public void setRepository(ShipRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Ship> getShips(Specification<Ship> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }

    @Override
    public Integer getShipsCount(Specification<Ship> specification) {
        return repository.findAll(specification).size();
    }

    @Override
    public Ship createShip(Ship ship) {
        ship.setRating(calculateRating(ship));
        return repository.saveAndFlush(ship);
    }

    @Override
    public void deleteById(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else throw new ShipNotFoundException();
    }

    @Override
    public Ship updateShip(Long id, Ship ship) {

        if (!repository.existsById(id)) throw new ShipNotFoundException();

        Ship shipFromDataBase = repository.findById(id).get();

        if (ship.getName() != null) shipFromDataBase.setName(ship.getName());
        if (ship.getPlanet() != null) shipFromDataBase.setPlanet(ship.getPlanet());
        if (ship.getShipType() != null) shipFromDataBase.setShipType(ship.getShipType());
        if (ship.getProdDate() != null) shipFromDataBase.setProdDate(ship.getProdDate());
        if (ship.getUsed() != null) shipFromDataBase.setUsed(ship.getUsed());
        if (ship.getSpeed() != null) shipFromDataBase.setSpeed(ship.getSpeed());
        if (ship.getCrewSize() != null) shipFromDataBase.setCrewSize(ship.getCrewSize());
        shipFromDataBase.setRating(calculateRating(shipFromDataBase));

        return repository.saveAndFlush(shipFromDataBase);
    }

    @Override
    public Ship getShipById(Long id) {
        if (repository.existsById(id)) {
            return repository.findById(id).get();
        } else throw new ShipNotFoundException();
    }

    @Override
    public Double calculateRating(Ship ship) {
        Double rating = (80 * ship.getSpeed() * (ship.getUsed() ? 0.5 : 1)) / (3019 - (ship.getProdDate().getYear() + 1900) + 1);
        rating = new BigDecimal(rating).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return rating;
    }

    public Specification<Ship> filterByName(String name) {
        return (Specification<Ship>) (root, query, criteriaBuilder) ->
            name == null ? null : criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), "%" + name.toUpperCase() + "%");
    }

    public Specification<Ship> filterByPlanet(String planet) {
        return (Specification<Ship>) (root, query, criteriaBuilder) ->
            planet == null ? null : criteriaBuilder.like(criteriaBuilder.upper(root.get("planet")), "%" + planet.toUpperCase() + "%");
    }

    public Specification<Ship> filterByShipType(ShipType shipType) {
        return (Specification<Ship>) (root, query, criteriaBuilder) ->
            shipType == null ? null : criteriaBuilder.equal(root.get("shipType"), shipType);
    }

    public Specification<Ship> filterByProdDate(Long after, Long before) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (after == null && before == null) return null;
            if (after != null && before == null) {
                Date date = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), date);
            }
            if (after == null) {
                Date date = new Date(before - 1);
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), date);
            }
            Date date1 = new Date(after);
            Date date2 = new Date(before - 1);
            return criteriaBuilder.between(root.get("prodDate"), date1, date2);
        };
        //если на странице выбрать, например, от 2995 до 2995 - корабль, год изготовления которого 2995, не отображается
        //2995 - 2996 тоже не отображается
        //2994 - 2995 отображается
    }

    public Specification<Ship> filterByUsage(Boolean isUsed) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (isUsed == null) return null;
            if (isUsed) return criteriaBuilder.isTrue(root.get("isUsed"));
            return criteriaBuilder.isFalse(root.get("isUsed"));
        };
    }

    public Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (minSpeed == null && maxSpeed == null) return null;
            if (minSpeed != null && maxSpeed == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);
            if (minSpeed == null) return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
            return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
        };
    }

    public Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (minCrewSize == null && maxCrewSize == null) return null;
            if (minCrewSize != null && maxCrewSize == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);
            if (minCrewSize == null) return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
            return criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
        };
    }

    public Specification<Ship> filterByRating(Double minRating, Double maxRating) {
        return (Specification<Ship>) (root, query, criteriaBuilder) -> {
            if (minRating == null && maxRating == null) return null;
            if (minRating != null && maxRating == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
            if (minRating == null) return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
            return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
        };
    }

}
