package ru.praktikum.mainservice.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationStorage locationStorage;

    @Override
    public Location createLocation(Location location) {

        log.info("Создаем новую локацию: location={}", location);
        return locationStorage.save(location);
    }
}
