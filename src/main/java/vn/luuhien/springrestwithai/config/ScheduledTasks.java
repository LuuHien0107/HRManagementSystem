package vn.luuhien.springrestwithai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.luuhien.springrestwithai.feature.auth.RefreshTokenRepository;

import java.time.Instant;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public ScheduledTasks(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        long deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.info("Cleanup expired refresh tokens completed. Deleted {} tokens", deletedCount);
    }
}
