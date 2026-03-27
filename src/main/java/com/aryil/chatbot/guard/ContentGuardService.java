package com.aryil.chatbot.guard;

import com.aryil.chatbot.policy.BannedPatternRepository;
import com.aryil.chatbot.policy.BannedPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ContentGuardService {

    private static final Logger log = LoggerFactory.getLogger(ContentGuardService.class);

    private final BannedPatternRepository repo;

    public ContentGuardService(BannedPatternRepository repo) {
        this.repo = repo;
    }

    public GuardResult checkUserMessage(String text) {
        return check(text, "USER");
    }

    public GuardResult checkAssistantMessage(String text) {
        return check(text, "ASSISTANT");
    }

    private GuardResult check(String text, String role) {
        String normalized = normalize(text);
        List<BannedPattern> rules = repo.findByEnabledTrue();

        for (BannedPattern rule : rules) {
            String type = safeUpper(rule.getType());
            String severity = safeUpper(rule.getSeverity());
            String patt = rule.getPattern();

            boolean matches = false;
            if ("KEYWORD".equals(type)) {
                matches = normalized.contains(normalize(patt));
            } else if ("REGEX".equals(type)) {
                Pattern compiled = Pattern.compile(patt, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                matches = compiled.matcher(text).find();
            }

            if (matches) {
                String reason = String.format("%s:%s", type, severity);

                if ("HIGH".equals(severity)) {
                    log.warn("Content BLOCKED - Role: {}, Severity: {}, Pattern: {}", role, severity, patt);
                    return GuardResult.block(reason);
                } else if ("MEDIUM".equals(severity)) {
                    log.warn("Content FLAGGED - Role: {}, Severity: {}, Pattern: {}", role, severity, patt);
                    return GuardResult.flag(reason);
                } else if ("LOW".equals(severity)) {
                    log.info("Content MONITORED - Role: {}, Severity: {}, Pattern: {}", role, severity, patt);
                }
            }
        }

        return GuardResult.allow();
    }

    private String normalize(String s) {
        if (s == null)
            return "";
        return s.toLowerCase(Locale.forLanguageTag("tr"))
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
