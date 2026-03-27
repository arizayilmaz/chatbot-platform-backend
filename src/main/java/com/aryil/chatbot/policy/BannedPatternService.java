package com.aryil.chatbot.policy;

import com.aryil.chatbot.common.exception.PatternDuplicateException;
import com.aryil.chatbot.common.exception.PatternNotFoundException;
import com.aryil.chatbot.policy.dto.CreatePatternRequest;
import com.aryil.chatbot.policy.dto.PatternDto;
import com.aryil.chatbot.policy.dto.UpdatePatternRequest;
import org.springframework.dao.DataIntegrityViolationException;
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
        try {
            BannedPattern p = BannedPattern.builder()
                    .pattern(req.pattern())
                    .type(req.type())
                    .enabled(true)
                    .severity(req.severity())
                    .notes(req.notes())
                    .build();

            return toDto(repo.save(p));
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("banned_patterns_pattern_key")) {
                throw new PatternDuplicateException("Pattern already exists");
            }
            throw e;
        }
    }

    @Transactional
    public PatternDto update(UUID id, UpdatePatternRequest req) {
        BannedPattern p = repo.findById(id)
                .orElseThrow(() -> new PatternNotFoundException("Pattern not found"));

        if (req.pattern() != null && !req.pattern().equals(p.getPattern())) {
            if (repo.existsByPattern(req.pattern())) {
                throw new PatternDuplicateException("Pattern already exists");
            }
            p.setPattern(req.pattern());
        }
        if (req.type() != null)
            p.setType(req.type());
        if (req.severity() != null)
            p.setSeverity(req.severity());
        if (req.enabled() != null)
            p.setEnabled(req.enabled());
        if (req.notes() != null)
            p.setNotes(req.notes());

        return toDto(repo.save(p));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw new PatternNotFoundException("Pattern not found");
        }
        repo.deleteById(id);
    }

    @Transactional
    public PatternDto setEnabled(UUID id, boolean enabled) {
        BannedPattern p = repo.findById(id)
                .orElseThrow(() -> new PatternNotFoundException("Pattern not found"));
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
                p.getCreatedAt());
    }
}
