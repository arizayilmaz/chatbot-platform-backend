package com.aryil.chatbot.guard;

import com.aryil.chatbot.policy.BannedPatternRepository;
import com.aryil.chatbot.policy.BannedPattern;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ContentGuardService {

    private final BannedPatternRepository repo;

    public ContentGuardService(BannedPatternRepository repo) {
        this.repo = repo;
    }

    public GuardResult checkUserMessage(String text) {
        String normalized = normalize(text);
        List<BannedPattern> rules = repo.findByEnabledTrue();

        for (BannedPattern rule : rules) {
            String type = safeUpper(rule.getType());
            String patt = rule.getPattern();

            if ("KEYWORD".equals(type)) {
                if (normalized.contains(normalize(patt))) {
                    return GuardResult.block("BANNED_KEYWORD:" + rule.getSeverity());
                }
            } else if ("REGEX".equals(type)) {
                Pattern compiled = Pattern.compile(patt, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                if (compiled.matcher(text).find()) {
                    return GuardResult.block("BANNED_REGEX:" + rule.getSeverity());
                }
            }
        }

        return GuardResult.allow();
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(new Locale("tr", "TR"))
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
    public GuardResult checkAssistantMessage(String text) {
        return checkUserMessage(text);
    }

}
