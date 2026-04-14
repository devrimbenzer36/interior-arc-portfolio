package com.portfolio.security.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding window rate limiter.
 *
 * Dış bağımlılık yok — ConcurrentHashMap + synchronized Deque ile çalışır.
 * Uygulama yeniden başlarsa sayaçlar sıfırlanır (portfolio için yeterli).
 *
 * Contact  : IP başına 3 istek / 24 saat
 * Login    : IP başına 5 başarısız deneme / 15 dakika → blok
 */
@Slf4j
@Component
public class RateLimiterService {

    private static final int      CONTACT_MAX    = 3;
    private static final Duration CONTACT_WINDOW = Duration.ofHours(24);

    private static final int    LOGIN_MAX      = 5;
    private static final Duration LOGIN_WINDOW  = Duration.ofMinutes(15);

    // Her IP için istek zamanlarını tutan map'ler
    private final ConcurrentHashMap<String, Deque<Instant>> contactAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<Instant>> loginFailures   = new ConcurrentHashMap<>();

    // ── Contact ───────────────────────────────────────────────────────────

    /**
     * @return true → istek kabul edilebilir | false → limit aşıldı (429 dön)
     */
    public boolean isContactAllowed(String ip) {
        return tryConsume(contactAttempts, ip, CONTACT_MAX, CONTACT_WINDOW);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    /**
     * IP bu an bloklu mu?
     * Her sorgu önce window dışı kayıtları temizler.
     */
    public boolean isLoginBlocked(String ip) {
        Deque<Instant> failures = loginFailures.get(ip);
        if (failures == null) return false;
        synchronized (failures) {
            purgeExpired(failures, LOGIN_WINDOW);
            return failures.size() >= LOGIN_MAX;
        }
    }

    /**
     * Başarısız login denemesini kaydet.
     */
    public void recordLoginFailure(String ip) {
        Deque<Instant> failures = loginFailures.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (failures) {
            failures.addLast(Instant.now());
            log.warn("Failed login attempt #{} from IP: {}", failures.size(), ip);
        }
    }

    /**
     * Başarılı login sonrası sayacı temizle.
     */
    public void clearLoginFailures(String ip) {
        loginFailures.remove(ip);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Window içindeki istek sayısı < max ise isteği kaydet ve true döner.
     * Aksi halde false döner (tüketim yapılmaz).
     */
    private boolean tryConsume(ConcurrentHashMap<String, Deque<Instant>> map,
                               String ip, int max, Duration window) {
        Deque<Instant> timestamps = map.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            purgeExpired(timestamps, window);
            if (timestamps.size() >= max) {
                return false;
            }
            timestamps.addLast(Instant.now());
            return true;
        }
    }

    /**
     * Window süresi dışına çıkmış eski kayıtları temizler.
     */
    private void purgeExpired(Deque<Instant> deque, Duration window) {
        Instant cutoff = Instant.now().minus(window);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
            deque.pollFirst();
        }
    }
}
