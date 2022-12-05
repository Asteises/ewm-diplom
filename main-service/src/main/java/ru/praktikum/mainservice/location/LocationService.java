package ru.praktikum.mainservice.location;

import org.springframework.stereotype.Service;

@Service
public interface LocationService {

    Location createLocation(Location location);
}
