package com.hostelapp.core.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates that a SQL string is a safe, read-only SELECT query,
 * then executes it via JdbcTemplate and returns the result rows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SafeQueryExecutor {

    /** Keywords that must never appear in a valid read-only query */
    private static final Set<String> BLOCKED_KEYWORDS = Set.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
            "TRUNCATE", "GRANT", "REVOKE", "EXEC", "EXECUTE", "MERGE",
            "CALL", "PRAGMA", "REPLACE", "LOAD", "IMPORT"
    );

    /** Simple check: the trimmed query must start with SELECT */
    private static final Pattern SELECT_PATTERN = Pattern.compile(
            "^\\s*SELECT\\b.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final JdbcTemplate jdbcTemplate;
    private final AiProperties aiProperties;

    /**
     * Validates and runs the SQL query, returning up to maxResultRows rows.
     *
     * @param sql the SQL to validate and execute
     * @return list of result rows (each row is a Map of column→value)
     * @throws IllegalArgumentException if the query is unsafe
     */
    public List<Map<String, Object>> execute(String sql) {
        validate(sql);

        // Append LIMIT if not already present to cap results
        String safeSql = appendLimit(sql, aiProperties.getMaxResultRows());

        log.info("Executing safe query: {}", safeSql);
        return jdbcTemplate.queryForList(safeSql);
    }

    private void validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("Query is empty.");
        }

        if (!SELECT_PATTERN.matcher(sql).matches()) {
            throw new IllegalArgumentException("Only SELECT queries are permitted.");
        }

        String upperSql = sql.toUpperCase();
        for (String blocked : BLOCKED_KEYWORDS) {
            // Match as a whole word to avoid false positives (e.g. 'created_at' contains 'create')
            Pattern wordPattern = Pattern.compile("\\b" + blocked + "\\b");
            if (wordPattern.matcher(upperSql).find()) {
                throw new IllegalArgumentException("Query contains blocked keyword: " + blocked);
            }
        }

        // Block comments that could be used to hide malicious SQL
        if (sql.contains("--") || sql.contains("/*")) {
            throw new IllegalArgumentException("SQL comments are not allowed.");
        }
    }

    private String appendLimit(String sql, int maxRows) {
        String trimmed = sql.trim();
        // Only append if there's no existing LIMIT clause
        if (!trimmed.toUpperCase().contains("LIMIT")) {
            // Remove trailing semicolon if present, then add LIMIT
            if (trimmed.endsWith(";")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
            }
            return trimmed + " LIMIT " + maxRows;
        }
        return trimmed;
    }
}
