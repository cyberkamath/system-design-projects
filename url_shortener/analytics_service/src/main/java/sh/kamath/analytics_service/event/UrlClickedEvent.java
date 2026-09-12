package sh.kamath.analytics_service.event;

import java.time.Instant;

public record UrlClickedEvent(
        String code,
        String originalUrl,
        Instant timestamp,
        String ipAddress,
        String userAgent,
        String referrer,
        String resolverNodeId,
        boolean wasInCache,
        long resolveTimeMs
) {}