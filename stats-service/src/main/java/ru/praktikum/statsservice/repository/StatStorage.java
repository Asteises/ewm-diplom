package ru.praktikum.statsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.praktikum.statsservice.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatStorage extends JpaRepository<EndpointHit, Long> {

    List<EndpointHit> findAllByCreatedBetween(LocalDateTime start, LocalDateTime end);

    List<EndpointHit> findAllByUriInAndCreatedBetween(List<String> uri, LocalDateTime start, LocalDateTime end);

    @Query("select eh from EndpointHit as eh where eh.uri = :uri")
    List<EndpointHit> findByUri(String uri);
}