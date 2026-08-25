package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ClipboardItem
import com.example.data.model.CodeFile
import com.example.data.model.Snippet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Snippet::class, ClipboardItem::class, CodeFile::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun snippetDao(): SnippetDao
    abstract fun clipboardDao(): ClipboardDao
    abstract fun codeFileDao(): CodeFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coding_keyboard_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val snippetDao = database.snippetDao()
            val codeFileDao = database.codeFileDao()

            val defaultSnippets = listOf(
                // Python Snippets
                Snippet(title = "main boilerplate", code = "if __name__ == '__main__':\n    main()", language = "Python", category = "Control", isFavorite = true),
                Snippet(title = "def function", code = "def func_name(arg1: int) -> None:\n    \"\"\"Docstring\"\"\"\n    pass", language = "Python", category = "Function", isFavorite = true),
                Snippet(title = "for in range", code = "for i in range(len(items)):\n    print(f\"Index {i}: {items[i]}\")", language = "Python", category = "Loop", isFavorite = false),
                Snippet(title = "try except", code = "try:\n    result = process()\nexcept Exception as e:\n    print(f\"Error: {e}\")", language = "Python", category = "Error Handling", isFavorite = false),
                Snippet(title = "class definition", code = "class MyClass:\n    def __init__(self, name: str):\n        self.name = name", language = "Python", category = "OOP", isFavorite = false),
                Snippet(title = "list comprehension", code = "[x * 2 for x in data if x > 0]", language = "Python", category = "Functional", isFavorite = false),

                // JavaScript Snippets
                Snippet(title = "console.log", code = "console.log('Value:', item);", language = "JavaScript", category = "Debug", isFavorite = true),
                Snippet(title = "arrow function", code = "const handleClick = (e) => {\n  console.log(e);\n};", language = "JavaScript", category = "Function", isFavorite = true),
                Snippet(title = "async await fetch", code = "async function fetchData(url) {\n  const res = await fetch(url);\n  return await res.json();\n}", language = "JavaScript", category = "Async", isFavorite = true),
                Snippet(title = "try catch", code = "try {\n  // code\n} catch (err) {\n  console.error(err);\n}", language = "JavaScript", category = "Error Handling", isFavorite = false),
                Snippet(title = "map filter", code = "const result = items.filter(x => x.active).map(x => x.id);", language = "JavaScript", category = "Array", isFavorite = false),
                Snippet(title = "useState hook", code = "const [state, setState] = useState(initialState);", language = "JavaScript", category = "React", isFavorite = false),

                // Kotlin Snippets
                Snippet(title = "fun main", code = "fun main() {\n    println(\"Hello, Coding Keyboard!\")\n}", language = "Kotlin", category = "Boilerplate", isFavorite = true),
                Snippet(title = "data class", code = "data class User(\n    val id: Long,\n    val name: String,\n    val email: String\n)", language = "Kotlin", category = "Model", isFavorite = true),
                Snippet(title = "composable fun", code = "@Composable\nfun MyComponent(modifier: Modifier = Modifier) {\n    // UI\n}", language = "Kotlin", category = "Compose", isFavorite = true),
                Snippet(title = "coroutine launch", code = "viewModelScope.launch {\n    // suspend task\n}", language = "Kotlin", category = "Coroutines", isFavorite = false),

                // C++ Snippets
                Snippet(title = "#include iostream", code = "#include <iostream>\n\nint main() {\n    std::cout << \"Hello World!\" << std::endl;\n    return 0;\n}", language = "C++", category = "Boilerplate", isFavorite = true),
                Snippet(title = "std::vector loop", code = "for (const auto& item : vec) {\n    std::cout << item << \" \";\n}", language = "C++", category = "Loop", isFavorite = false),

                // HTML / CSS Snippets
                Snippet(title = "HTML5 Boilerplate", code = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"UTF-8\">\n  <title>Title</title>\n</head>\n<body>\n  \n</body>\n</html>", language = "HTML", category = "Structure", isFavorite = true),
                Snippet(title = "div container", code = "<div class=\"flex items-center justify-between p-4\">\n  \n</div>", language = "HTML", category = "Tags", isFavorite = false),

                // SQL Snippets
                Snippet(title = "SELECT WHERE", code = "SELECT * FROM users WHERE status = 'active' ORDER BY created_at DESC;", language = "SQL", category = "Query", isFavorite = true),
                Snippet(title = "JOIN Query", code = "SELECT u.name, o.total FROM users u\nJOIN orders o ON u.id = o.user_id\nWHERE o.total > 100;", language = "SQL", category = "Query", isFavorite = false),

                // Bash / Shell
                Snippet(title = "shebang & strict mode", code = "#!/bin/bash\nset -euo pipefail\n\necho \"Script starting...\"", language = "Bash", category = "Script", isFavorite = true),
                Snippet(title = "check file exists", code = "if [ -f \"\$FILE\" ]; then\n    echo \"File exists.\"\nfi", language = "Bash", category = "Condition", isFavorite = false)
            )
            snippetDao.insertAll(defaultSnippets)

            val starterFiles = listOf(
                CodeFile(
                    name = "main.py",
                    language = "Python",
                    content = """# Interactive Coding Keyboard Sandbox
def fibonacci(n: int) -> list[int]:
    sequence = [0, 1]
    while len(sequence) < n:
        sequence.append(sequence[-1] + sequence[-2])
    return sequence[:n]

print("Fibonacci series:")
print(fibonacci(10))
"""
                ),
                CodeFile(
                    name = "script.js",
                    language = "JavaScript",
                    content = """// JavaScript Sandbox with Live Execution
function quickCodeTest() {
  const languages = ["Python", "JavaScript", "Kotlin", "C++", "Rust"];
  const formatted = languages.map((lang, idx) => (idx + 1) + ". " + lang.toUpperCase());
  
  console.log("Supported Languages in Coding Keyboard:");
  formatted.forEach(item => console.log(item));
  
  return { count: languages.length, status: "Ready" };
}

quickCodeTest();
"""
                ),
                CodeFile(
                    name = "app.kt",
                    language = "Kotlin",
                    content = """package com.example

fun main() {
    val features = listOf(
        "Full Modifier Keys (Ctrl, Alt, Shift, Fn)",
        "Dedicated Quick-Symbol Strip (->, =>, {}, [])",
        "Multi-line Selection & Cursor Gestures",
        "Code Snippet Engine & Custom Macros",
        "Multi-Language Live Sandbox & Terminal"
    )
    
    println("Coding Keyboard Features:")
    features.forEachIndexed { i, feat -> println("[${'$'}{i + 1}] ${'$'}feat") }
}
"""
                ),
                CodeFile(
                    name = "index.html",
                    language = "HTML",
                    content = """<!DOCTYPE html>
<html>
<head>
  <style>
    body { background: #0f172a; color: #38bdf8; font-family: monospace; padding: 20px; }
    h1 { color: #f43f5e; border-bottom: 2px solid #334155; padding-bottom: 8px; }
    .badge { background: #1e293b; padding: 4px 8px; border-radius: 4px; color: #a855f7; }
  </style>
</head>
<body>
  <h1>Coding Keyboard</h1>
  <p>Status: <span class="badge">IDE Active</span></p>
  <p>Ready to code directly on mobile device with zero friction.</p>
</body>
</html>
"""
                )
            )
            codeFileDao.insertAll(starterFiles)
        }
    }
}
