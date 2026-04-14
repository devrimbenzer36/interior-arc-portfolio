package com.portfolio.domain.contact.service;

import com.portfolio.domain.contact.dto.request.SendContactMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Contact form gönderimlerini Resend HTTP API ile iletir.
 * SMTP yerine HTTP (port 443) — Railway'de port kısıtlaması yok.
 */
@Slf4j
@Service
public class EmailNotificationService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.notification.recipients}")
    private String recipientsRaw;

    private static final Pattern REPEAT_PATTERN = Pattern.compile("(.)\\1{5,}");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")
                    .withLocale(new java.util.Locale("tr", "TR"));

    private final RestClient restClient;

    public EmailNotificationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Async("auditExecutor")
    public void sendContactNotification(SendContactMessageRequest request) {
        log.warn("[EMAIL] sendContactNotification started for: {}", request.getEmail());
        if (resendApiKey == null || resendApiKey.isBlank() || "not-configured".equals(resendApiKey)) {
            log.warn("[EMAIL] RESEND_API_KEY not configured — skipping notification");
            return;
        }
        try {
            List<String> recipients = Arrays.stream(recipientsRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            boolean spam = looksLikeSpam(request);
            String subject = spam
                    ? "⚠ [ŞÜPHELI] Yeni Mesaj — " + senderLabel(request)
                    : "✉ Yeni Mesaj — " + senderLabel(request);

            Map<String, Object> body = Map.of(
                    "from", "Interior Arc Studio <onboarding@resend.dev>",
                    "to", recipients,
                    "subject", subject,
                    "html", buildHtml(request)
            );

            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.warn("[EMAIL] Resend API call succeeded for: {}", request.getEmail());

        } catch (Exception e) {
            log.warn("[EMAIL] Resend API call FAILED for {}: {} — {}", request.getEmail(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ── HTML şablonu ──────────────────────────────────────────────────────

    private String buildHtml(SendContactMessageRequest req) {
        String now = ZonedDateTime.now(ZoneId.of("Europe/Istanbul")).format(FORMATTER);
        String subject = req.getSubject() != null && !req.getSubject().isBlank()
                ? escHtml(req.getSubject())
                : "<span style='color:#9e9e9e;font-style:italic;'>Belirtilmedi</span>";
        String phone = req.getPhone() != null && !req.getPhone().isBlank()
                ? escHtml(req.getPhone())
                : "<span style='color:#9e9e9e;font-style:italic;'>Belirtilmedi</span>";

        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1"/>
                </head>
                <body style="margin:0;padding:0;background:#F5F4F2;font-family:'Helvetica Neue',Arial,sans-serif;">

                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F5F4F2;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0"
                               style="background:#FFFFFF;border:1px solid #E8E5E0;max-width:560px;width:100%%;">

                          <tr>
                            <td style="background:#1C1C1A;padding:28px 36px;">
                              <p style="margin:0;color:#FAFAF8;font-size:11px;letter-spacing:4px;
                                         text-transform:uppercase;font-weight:400;">Interior Arc</p>
                              <p style="margin:6px 0 0;color:#8B7355;font-size:13px;
                                         letter-spacing:2px;text-transform:uppercase;">Yeni Mesaj</p>
                            </td>
                          </tr>

                          <tr><td style="height:3px;background:#8B7355;"></td></tr>

                          <tr>
                            <td style="padding:36px 36px 0;">
                              <table width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;">
                                %s
                                %s
                                %s
                                %s
                              </table>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:28px 36px 0;">
                              <p style="margin:0 0 10px;font-size:10px;color:#8B7355;
                                         letter-spacing:3px;text-transform:uppercase;">Mesaj</p>
                              <div style="background:#FAFAF8;border-left:3px solid #8B7355;
                                           padding:16px 20px;font-size:14px;color:#1C1C1A;
                                           line-height:1.7;white-space:pre-wrap;">%s</div>
                            </td>
                          </tr>

                          <tr>
                            <td style="padding:28px 36px 36px;">
                              <p style="margin:0;font-size:11px;color:#9e9e9e;">%s tarihinde iletildi</p>
                            </td>
                          </tr>

                          <tr>
                            <td style="background:#F5F4F2;padding:18px 36px;border-top:1px solid #E8E5E0;">
                              <p style="margin:0;font-size:10px;color:#9e9e9e;
                                         letter-spacing:2px;text-transform:uppercase;">
                                Elif Benzer — Interior Arc
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>

                </body>
                </html>
                """.formatted(
                infoRow("Ad Soyad", escHtml(req.getFullName())),
                infoRow("E-posta",  "<a href='mailto:" + escHtml(req.getEmail()) + "' "
                                  + "style='color:#8B7355;text-decoration:none;'>"
                                  + escHtml(req.getEmail()) + "</a>"),
                infoRow("Telefon",  phone),
                infoRow("Konu",     subject),
                escHtml(req.getMessage()),
                now
        );
    }

    private String infoRow(String label, String value) {
        return """
                <tr>
                  <td style="padding:8px 0;border-bottom:1px solid #F0EDE8;font-size:10px;
                              color:#8B7355;letter-spacing:2px;text-transform:uppercase;
                              width:110px;vertical-align:top;">%s</td>
                  <td style="padding:8px 0 8px 16px;border-bottom:1px solid #F0EDE8;
                              font-size:14px;color:#1C1C1A;">%s</td>
                </tr>
                """.formatted(label, value);
    }

    private String senderLabel(SendContactMessageRequest req) {
        return req.getFullName() != null ? req.getFullName() : req.getEmail();
    }

    private boolean looksLikeSpam(SendContactMessageRequest req) {
        String msg = req.getMessage();
        if (msg == null || msg.length() < 20) return false;
        long upperCount  = msg.chars().filter(Character::isUpperCase).count();
        long letterCount = msg.chars().filter(Character::isLetter).count();
        if (letterCount > 15 && (double) upperCount / letterCount > 0.80) return true;
        if (REPEAT_PATTERN.matcher(msg).find()) return true;
        String lower = msg.toLowerCase();
        if (lower.contains("http://") || lower.contains("https://") || lower.contains("www.")) return true;
        return false;
    }

    private String escHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }
}
