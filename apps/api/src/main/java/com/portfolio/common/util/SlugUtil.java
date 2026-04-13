package com.portfolio.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * SEO-friendly slug üretici.
 *
 * "Boğaziçi Dairesi 2024" → "bogazici-dairesi-2024"
 *
 * Türkçe karakterleri ASCII karşılıklarına dönüştürür,
 * özel karakterleri temizler, boşlukları tire ile değiştirir.
 */
public final class SlugUtil {

    private static final Pattern NON_ASCII_PATTERN = Pattern.compile("[^a-z0-9-]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");

    // Türkçe karakterler için özel mapping — Normalizer tek başına yeterli değil
    private static final String[][] TR_CHAR_MAP = {
            {"ş", "s"}, {"Ş", "s"},
            {"ğ", "g"}, {"Ğ", "g"},
            {"ü", "u"}, {"Ü", "u"},
            {"ö", "o"}, {"Ö", "o"},
            {"ı", "i"}, {"İ", "i"},
            {"ç", "c"}, {"Ç", "c"}
    };

    private SlugUtil() {}

    /**
     * Verilen metinden URL-safe slug üretir.
     *
     * @param input ham metin (proje başlığı vb.)
     * @return slug — boşsa boş string döner
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String result = input.trim().toLowerCase(Locale.ENGLISH);

        // Türkçe karakterleri dönüştür
        for (String[] mapping : TR_CHAR_MAP) {
            result = result.replace(mapping[0], mapping[1]);
        }

        // Unicode normalizasyonu (diğer aksan karakterleri için)
        result = Normalizer.normalize(result, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Boşlukları tire yap
        result = result.replace(" ", "-");

        // İzin verilmeyen karakterleri temizle
        result = NON_ASCII_PATTERN.matcher(result).replaceAll("");

        // Ardışık tireleri tek tireye indir
        result = MULTIPLE_HYPHENS.matcher(result).replaceAll("-");

        // Baş ve sondaki tireleri temizle
        return result.replaceAll("^-|-$", "");
    }
}
