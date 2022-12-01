package ru.praktikum.mainservice.location;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationStorage extends JpaRepository<Location, Long> {

}
