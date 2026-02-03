package com.aryil.chatbot.policy;

import com.aryil.chatbot.policy.dto.CreatePatternRequest;
import com.aryil.chatbot.policy.dto.PatternDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BannedPatternService {

    private final BannedPatternRepository repo;

    public BannedPatternService(BannedPatternRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<PatternDto> list() {
        return repo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PatternDto create(CreatePatternRequest req) {
        BannedPattern p = BannedPattern.builder()
                .pattern(req.pattern())
                .type(req.type())
                .enabled(true)
                .severity(req.severity())
                .notes(req.notes())
                .build();

        return toDto(repo.save(p));
    }

    @Transactional
    public PatternDto setEnabled(UUID id, boolean enabled) {
        BannedPattern p = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pattern not found: " + id));
        p.setEnabled(enabled);
        return toDto(repo.save(p));
    }

    private PatternDto toDto(BannedPattern p) {
        return new PatternDto(
                p.getId(),
                p.getPattern(),
                p.getType(),
                p.isEnabled(),
                p.getSeverity(),
                p.getNotes(),
                p.getCreatedAt()
        );
    }
}
