package com.aryil.chatbot.policy;

import com.aryil.chatbot.policy.dto.CreatePatternRequest;
import com.aryil.chatbot.policy.dto.PatternDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminPolicyController {

    private final BannedPatternService service;

    public AdminPolicyController(BannedPatternService service) {
        this.service = service;
    }

    @GetMapping("/patterns")
    public List<PatternDto> list() {
        return service.list();
    }

    @PostMapping("/patterns")
    public PatternDto create(@Valid @RequestBody CreatePatternRequest req) {
        return service.create(req);
    }

    @PutMapping("/patterns/{id}/toggle")
    public PatternDto toggle(@PathVariable UUID id, @RequestParam boolean enabled) {
        return service.setEnabled(id, enabled);
    }
}
