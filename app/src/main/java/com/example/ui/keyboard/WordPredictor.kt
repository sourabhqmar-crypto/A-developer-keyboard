package com.example.ui.keyboard

object WordPredictor {

    private val commonWords = listOf(
        // High frequency English words
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "hello", "hey", "thanks", "thank", "please", "yes", "great", "awesome", "perfect", "cool",
        "today", "tomorrow", "tonight", "morning", "night", "always", "never", "sometimes", "maybe", "sure",
        "help", "need", "love", "like", "feel", "try", "call", "send", "message", "check",
        "fine", "done", "ready", "started", "working", "going", "doing", "having", "waiting", "looking",
        "happy", "busy", "free", "available", "important", "question", "answer", "problem", "solution", "idea",
        "meeting", "phone", "email", "address", "number", "link", "website", "project", "code", "app",
        "developer", "programming", "software", "system", "feature", "issue", "bug", "release", "build", "test",

        // Programming keywords & tech tokens
        "const", "let", "var", "val", "fun", "function", "class", "interface", "object", "enum",
        "public", "private", "protected", "internal", "override", "abstract", "final", "open",
        "import", "package", "export", "default", "return", "throw", "try", "catch", "finally",
        "async", "await", "suspend", "coroutine", "flow", "state", "mutable", "remember", "composable",
        "if", "else", "when", "switch", "case", "for", "while", "do", "break", "continue",
        "true", "false", "null", "undefined", "this", "super", "self", "typeof", "instanceof",
        "string", "number", "boolean", "int", "long", "float", "double", "list", "map", "set",
        "array", "json", "http", "api", "url", "data", "model", "view", "viewmodel", "repository",
        "activity", "fragment", "service", "context", "intent", "modifier", "column", "row", "box",
        "text", "button", "card", "icon", "scaffold", "surface", "spacer", "lazy", "items"
    )

    private val nextWordMap: Map<String, List<String>> = mapOf(
        "i" to listOf("am", "have", "will", "think", "can", "would", "want"),
        "you" to listOf("are", "can", "have", "want", "know", "need", "should"),
        "we" to listOf("are", "have", "can", "will", "need", "should"),
        "they" to listOf("are", "have", "will", "were", "can"),
        "it" to listOf("is", "was", "will", "looks", "works", "seems"),
        "he" to listOf("is", "was", "will", "said", "has"),
        "she" to listOf("is", "was", "will", "said", "has"),
        "how" to listOf("are", "is", "do", "can", "about", "to"),
        "what" to listOf("is", "are", "do", "time", "about", "if"),
        "thank" to listOf("you", "you so much", "very much"),
        "thanks" to listOf("for", "a lot", "again", "bro"),
        "good" to listOf("morning", "afternoon", "evening", "night", "job", "luck", "idea"),
        "see" to listOf("you", "you later", "you soon", "what"),
        "let" to listOf("me", "us", "it", "know"),
        "please" to listOf("let", "find", "check", "send", "confirm"),
        "val" to listOf("name", "id", "data", "result", "state", "context"),
        "var" to listOf("count", "index", "total", "flag", "text"),
        "fun" to listOf("main", "onCreate", "setup", "render", "update"),
        "class" to listOf("MainActivity", "ViewModel", "Repository", "Model"),
        "import" to listOf("androidx", "kotlinx", "android", "java", "com"),
        "override" to listOf("fun", "val", "var"),
        "private" to listOf("val", "var", "fun", "lateinit"),
        "public" to listOf("val", "var", "fun", "class")
    )

    fun getSuggestions(currentPrefix: String, lastWord: String = "", limit: Int = 3): List<String> {
        val trimmed = currentPrefix.trim().lowercase()

        // If user hasn't started typing a word, show next-word predictions based on previous word
        if (trimmed.isEmpty()) {
            val prevTrimmed = lastWord.trim().lowercase()
            if (prevTrimmed.isNotEmpty()) {
                val nexts = nextWordMap[prevTrimmed]
                if (!nexts.isNullOrEmpty()) {
                    return nexts.take(limit)
                }
            }
            return listOf("I", "The", "Thanks")
        }

        // Exact & prefix matches
        val prefixMatches = commonWords.filter { it.startsWith(trimmed) }

        val result = mutableListOf<String>()
        
        // If the current prefix is not an exact word, put the typed word itself as first option if capitalize or raw
        if (!commonWords.contains(trimmed)) {
            result.add(currentPrefix)
        }

        prefixMatches.forEach { word ->
            if (result.size < limit && !result.any { it.equals(word, ignoreCase = true) }) {
                // Match casing of prefix
                val formatted = if (currentPrefix.isNotEmpty() && currentPrefix.first().isUpperCase()) {
                    word.replaceFirstChar { it.uppercase() }
                } else {
                    word
                }
                result.add(formatted)
            }
        }

        // Fill remaining if needed
        if (result.size < limit) {
            val containsMatches = commonWords.filter { it.contains(trimmed) && !result.contains(it) }
            for (match in containsMatches) {
                if (result.size >= limit) break
                result.add(match)
            }
        }

        return result.take(limit)
    }
}
