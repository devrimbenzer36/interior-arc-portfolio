package com.portfolio.domain.contact.service;

import com.portfolio.domain.contact.dto.request.SendContactMessageRequest;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Contact form gönderimlerini e-posta olarak iletir.
 *
 * @Async: Mail gönderimi SMTP network çağrısı içerir (~200-500ms).
 * Ana iş akışını bloke etmemesi için async çalışır.
 * Hata olursa loglanır — ziyaretçiye hata gösterilmez (mesaj DB'ye zaten kaydedildi).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    // Gönderen — interiorarcstudio@gmail.com (MAIL_USERNAME env variable)
    @Value("${spring.mail.username}")
    private String fromAddress;

    // Alıcılar — virgülle ayrılmış liste yml'den okunur
    @Value("#{'${app.notification.recipients}'.split(',')}")
    private List<String> recipients;

    // Compile once — tekrarlayan karakter tespiti için (ReDoS riski olmadan)
    private static final Pattern REPEAT_PATTERN = Pattern.compile("(.)\\1{5,}");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")
                    .withLocale(new java.util.Locale("tr", "TR"));

    @Async("auditExecutor")
    public void sendContactNotification(SendContactMessageRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, "Interior Arc Studio");
            helper.setTo(recipients.toArray(new String[0]));

            String subject = looksLikeSpam(request)
                    ? "⚠ [ŞÜPHELI] Yeni Mesaj — " + senderLabel(request)
                    : "✉ Yeni Mesaj — " + senderLabel(request);
            helper.setSubject(subject);
            helper.setText(buildHtml(request), true);

            mailSender.send(message);
            log.info("Contact notification sent to {} for sender {}", recipients, request.getEmail());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Contact notification email failed for {}: {}", request.getEmail(), e.getMessage());
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

                          <!-- Üst başlık -->
                          <tr>
                            <td style="background:#1C1C1A;padding:28px 36px;">
                              <p style="margin:0;color:#FAFAF8;font-size:11px;letter-spacing:4px;
                                         text-transform:uppercase;font-weight:400;">
                                Interior Arc
                              </p>
                              <p style="margin:6px 0 0;color:#8B7355;font-size:13px;
                                         letter-spacing:2px;text-transform:uppercase;">
                                Yeni Mesaj
                              </p>
                            </td>
                          </tr>

                          <!-- İnce aksan çizgisi -->
                          <tr>
                            <td style="height:3px;background:#8B7355;"></td>
                          </tr>

                          <!-- İçerik -->
                          <tr>
                            <td style="padding:36px 36px 0;">

                              <!-- Gönderen bilgileri -->
                              <table width="100%%" cellpadding="0" cellspacing="0"
                                     style="border-collapse:collapse;">
                                %s
                                %s
                                %s
                                %s
                              </table>

                            </td>
                          </tr>

                          <!-- Mesaj -->
                          <tr>
                            <td style="padding:28px 36px 0;">
                              <p style="margin:0 0 10px;font-size:10px;color:#8B7355;
                                         letter-spacing:3px;text-transform:uppercase;">
                                Mesaj
                              </p>
                              <div style="background:#FAFAF8;border-left:3px solid #8B7355;
                                           padding:16px 20px;font-size:14px;color:#1C1C1A;
                                           line-height:1.7;white-space:pre-wrap;">
                                %s
                              </div>
                            </td>
                          </tr>

                          <!-- Alt bilgi -->
                          <tr>
                            <td style="padding:28px 36px 36px;">
                              <p style="margin:0;font-size:11px;color:#9e9e9e;">
                                %s tarihinde iletildi
                              </p>
                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="background:#F5F4F2;padding:18px 36px;
                                        border-top:1px solid #E8E5E0;">
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
                infoRow("Ad Soyad",  escHtml(req.getFullName())),
                infoRow("E-posta",   "<a href='mailto:" + escHtml(req.getEmail()) + "' "
                                   + "style='color:#8B7355;text-decoration:none;'>"
                                   + escHtml(req.getEmail()) + "</a>"),
                infoRow("Telefon",   phone),
                infoRow("Konu",      subject),
                escHtml(req.getMessage()),
                now
        );
    }

    /** Tek bilgi satırı — etiket + değer */
    private String infoRow(String label, String value) {
        return """
                <tr>
                  <td style="padding:8px 0;border-bottom:1px solid #F0EDE8;
                              font-size:10px;color:#8B7355;letter-spacing:2px;
                              text-transform:uppercase;width:110px;vertical-align:top;">
                    %s
                  </td>
                  <td style="padding:8px 0 8px 16px;border-bottom:1px solid #F0EDE8;
                              font-size:14px;color:#1C1C1A;">
                    %s
                  </td>
                </tr>
                """.formatted(label, value);
    }

    private String senderLabel(SendContactMessageRequest req) {
        return req.getFullName() != null ? req.getFullName() : req.getEmail();
    }

    /**
     * Basit spam/bot tespiti — mail subject'ine uyarı ekler.
     * Mesaj DB'ye kaydedilir, silinmez; sadece email işaretlenir.
     *
     * Tespit kriterleri:
     *  - Metinin %80'inden fazlası büyük harf (all-caps saldırganlık / bot)
     *  - 6+ art arda aynı karakter (aaaaaaa — bot test mesajı)
     *  - HTTP/HTTPS URL içeriği (reklam spam)
     */
    private boolean looksLikeSpam(SendContactMessageRequest req) {
        String msg = req.getMessage();
        if (msg == null || msg.length() < 20) return false;

        // %80+ büyük harf kontrolü
        long upperCount  = msg.chars().filter(Character::isUpperCase).count();
        long letterCount = msg.chars().filter(Character::isLetter).count();
        if (letterCount > 15 && (double) upperCount / letterCount > 0.80) return true;

        // 6+ art arda tekrarlayan karakter — .find() kullan, .matches() değil (ReDoS önlemi)
        if (REPEAT_PATTERN.matcher(msg).find()) return true;

        // URL spam
        String lower = msg.toLowerCase();
        if (lower.contains("http://") || lower.contains("https://") || lower.contains("www.")) return true;

        return false;
    }

    /** XSS koruması — HTML özel karakterleri escape et */
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
