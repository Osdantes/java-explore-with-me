package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto compilationDto) {
        return CompilationMapper.toCompilationDto(compilationRepository
                .save(addEventsInCompilation(CompilationMapper.toCompilation(compilationDto), compilationDto.getEvents())));
    }

    @Override
    public CompilationDto updateCompilation(int compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException(compId, "Category with id " + compId + " was not found"));

        return CompilationMapper.toCompilationDto(compilationRepository
                .save(CompilationMapper.toCompilation(compilationDto,
                        addEventsInCompilation(compilation, compilationDto.getEvents()))));
    }

    @Override
    public void deleteCompilation(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException(compId, "Category with id " + compId + " was not found"));
        compilation.getEvents().forEach(compilation::removeEvent);

        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
        }

        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(int compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new ObjectNotFoundException(compId, "Category with id " + compId + " was not found"));
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    private Compilation addEventsInCompilation(Compilation compilation, Set<Integer> eventsId) {
        compilation.setEvents(new HashSet<>());

        if (eventsId != null && !eventsId.isEmpty()) {
            eventsId.forEach((eventId) -> {
                Event event = eventRepository
                        .findById(eventId)
                        .orElseThrow(() -> new ObjectNotFoundException(eventId, "Event with id{} was not found"));

                compilation.addEvent(event);
            });
        }
        return compilation;
    }
}