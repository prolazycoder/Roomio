package com.hostelapp.core.whatsapp;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a JDBC query result (List of Maps) into a human-readable
 * plain-text table suitable for a WhatsApp message.
 */
@Component
public class ResultFormatter {

    private static final int MAX_CELL_WIDTH = 20;
    private static final int MAX_WHATSAPP_LENGTH = 1500;

    /**
     * Formats query results as a plain-text table.
     *
     * @param rows the result rows from JDBC
     * @return formatted string, or a "no results" message
     */
    public String format(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "✅ Query ran successfully but returned no results.";
        }

        List<String> columns = rows.get(0).keySet().stream().toList();
        int rowCount = rows.size();

        StringBuilder sb = new StringBuilder();
        sb.append("📊 *Query Results* (").append(rowCount).append(" row").append(rowCount == 1 ? "" : "s").append(")\n\n");

        // Calculate column widths
        int[] widths = columns.stream()
                .mapToInt(col -> {
                    int maxDataWidth = rows.stream()
                            .mapToInt(row -> String.valueOf(row.getOrDefault(col, "")).length())
                            .max().orElse(0);
                    return Math.min(MAX_CELL_WIDTH, Math.max(col.length(), maxDataWidth));
                }).toArray();

        // Header row
        sb.append(buildRow(columns, widths));
        sb.append(buildSeparator(widths));

        // Data rows
        for (Map<String, Object> row : rows) {
            List<String> cells = columns.stream()
                    .map(col -> truncate(String.valueOf(row.getOrDefault(col, "")), MAX_CELL_WIDTH))
                    .collect(Collectors.toList());
            sb.append(buildRow(cells, widths));
        }

        String result = sb.toString();

        // Truncate for WhatsApp message limit
        if (result.length() > MAX_WHATSAPP_LENGTH) {
            result = result.substring(0, MAX_WHATSAPP_LENGTH - 50)
                    + "\n\n_(Results truncated — too many rows)_";
        }

        return result;
    }

    /**
     * Formats a simple error reply.
     */
    public String formatError(String reason) {
        return "⚠️ I couldn't answer that query.\n\n*Reason:* " + reason
                + "\n\nTry asking something like:\n"
                + "  • _How many vacant rooms are there?_\n"
                + "  • _List all active leases_\n"
                + "  • _What invoices are overdue?_";
    }

    private String buildRow(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder("| ");
        for (int i = 0; i < cells.size(); i++) {
            sb.append(padRight(truncate(cells.get(i), widths[i]), widths[i]));
            sb.append(" | ");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("|");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String padRight(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}
