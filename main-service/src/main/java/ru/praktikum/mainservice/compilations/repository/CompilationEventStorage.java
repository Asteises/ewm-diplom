package ru.praktikum.mainservice.compilations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.praktikum.mainservice.compilations.model.Compilation;
import ru.praktikum.mainservice.compilations.model.CompilationEvent;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompilationEventStorage extends JpaRepository<CompilationEvent, Long> {

    Optional<CompilationEvent> findByEvent_IdAndAndComp_Id(long eventId, long compId);

    List<CompilationEvent> findAllByComp(Compilation compilation);
}
