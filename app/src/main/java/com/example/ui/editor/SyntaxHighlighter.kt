package com.example.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.KeyboardThemeConfig

object SyntaxHighlighter {

    private val KEYWORDS = setOf(
        "fun", "val", "var", "def", "class", "interface", "object", "return", "if", "else", "while",
        "for", "in", "when", "switch", "case", "break", "continue", "import", "package", "public",
        "private", "protected", "override", "const", "let", "function", "async", "await", "try",
        "catch", "finally", "throw", "new", "this", "super", "typeof", "instanceof", "true", "false",
        "null", "nil", "None", "True", "False", "lambda", "yield", "export", "default", "from",
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "UPDATE", "DELETE", "JOIN", "ON", "ORDER", "BY",
        "GROUP", "HAVING", "CREATE", "TABLE", "DROP", "ALTER", "PRIMARY", "KEY", "AND", "OR", "NOT"
    )

    private val TYPES = setOf(
        "Int", "String", "Boolean", "Double", "Float", "Long", "List", "Map", "Set", "Array",
        "Unit", "Any", "void", "int", "char", "bool", "double", "float", "long", "short",
        "number", "string", "boolean", "object", "any", "unknown", "never", "vector", "std"
    )

    fun highlight(code: String, theme: KeyboardThemeConfig): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            val keywordColor = when (theme.id) {
                "monokai" -> Color(0xFFF92672) // Pink
                "cyberpunk" -> Color(0xFFFF007F) // Neon pink
                "dracula" -> Color(0xFFFF79C6) // Pink
                "matrix" -> Color(0xFF58A6FF) // Blue-ish cyan
                "github_light" -> Color(0xFFCF222E) // Red
                else -> Color(0xFF569CD6) // VS Code Blue
            }

            val stringColor = when (theme.id) {
                "monokai" -> Color(0xFFE6DB74) // Yellow
                "cyberpunk" -> Color(0xFFFFE600) // Neon Yellow
                "dracula" -> Color(0xFFF1FA8C) // Yellow
                "matrix" -> Color(0xFF7EE787) // Light Green
                "github_light" -> Color(0xFF0A3069) // Deep blue
                else -> Color(0xFFCE9178) // Orange-brown
            }

            val commentColor = when (theme.id) {
                "matrix" -> Color(0xFF238636)
                else -> Color(0xFF6A9955)
            }

            val numberColor = when (theme.id) {
                "monokai" -> Color(0xFFAE81FF) // Purple
                "cyberpunk" -> Color(0xFF00F0FF) // Cyan
                "dracula" -> Color(0xFFBD93F9) // Purple
                else -> Color(0xFFB5CEA8) // Light green
            }

            val typeColor = when (theme.id) {
                "monokai" -> Color(0xFF66D9EF) // Cyan
                "cyberpunk" -> Color(0xFF7928CA) // Violet
                "dracula" -> Color(0xFF8BE9FD) // Cyan
                else -> Color(0xFF4EC9B0) // Teal
            }

            // 1. Comments (//, /* */, #, --)
            val commentRegex = Regex("(//.*$)|(/\\*.*?\\*/)|(#.*$)|(--.*$)", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
            for (match in commentRegex.findAll(code)) {
                addStyle(SpanStyle(color = commentColor, fontWeight = FontWeight.Normal), match.range.first, match.range.last + 1)
            }

            // 2. Strings ("...", '...', `...`)
            val stringRegex = Regex("(\"[^\"]*\")|('[^']*')|(`[^`]*`)")
            for (match in stringRegex.findAll(code)) {
                addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
            }

            // 3. Numbers (0x12, 123.45, etc.)
            val numberRegex = Regex("\\b(0x[0-9a-fA-F]+|0b[01]+|\\d+\\.?\\d*)\\b")
            for (match in numberRegex.findAll(code)) {
                addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
            }

            // 4. Identifiers & Keywords
            val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
            for (match in wordRegex.findAll(code)) {
                val word = match.value
                if (KEYWORDS.contains(word)) {
                    addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                } else if (TYPES.contains(word)) {
                    addStyle(SpanStyle(color = typeColor, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
                }
            }
        }
    }
}
