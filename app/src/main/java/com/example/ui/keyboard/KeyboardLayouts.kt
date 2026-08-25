package com.example.ui.keyboard

import com.example.data.model.KeyboardLayoutMode

object KeyboardLayouts {

    fun getGboardQwerty(
        isShifted: Boolean,
        isCapsLocked: Boolean,
        showNumberRow: Boolean
    ): List<List<KeyModel>> {
        val rows = mutableListOf<List<KeyModel>>()

        // Optional Dedicated Number Row (Gboard setting)
        if (showNumberRow) {
            val numKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { num ->
                KeyModel(
                    primaryLabel = num,
                    action = KeyAction.InsertText(num),
                    weight = 1.0f
                )
            }
            rows.add(numKeys)
        }

        // Row 1: q w e r t y u i o p with secondary number hints
        val row1Letters = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val row1Numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val row1Popups = listOf(
            listOf("q", "1", "+", "="),
            listOf("w", "2", "w"),
            listOf("e", "3", "é", "è", "ê", "ë", "€"),
            listOf("r", "4", "r", "®"),
            listOf("t", "5", "t", "™"),
            listOf("y", "6", "y", "¥"),
            listOf("u", "7", "ú", "ù", "û", "ü"),
            listOf("i", "8", "í", "ì", "î", "ï"),
            listOf("o", "9", "ó", "ò", "ô", "ö", "œ"),
            listOf("p", "0", "p", "π")
        )

        val row1 = row1Letters.mapIndexed { idx, letter ->
            val isUpper = isShifted || isCapsLocked
            val primary = if (isUpper) letter.uppercase() else letter
            val secondary = if (!showNumberRow) row1Numbers[idx] else null
            KeyModel(
                primaryLabel = primary,
                secondaryLabel = secondary,
                action = KeyAction.InsertText(primary),
                secondaryAction = if (secondary != null) KeyAction.InsertText(secondary) else null,
                popupOptions = row1Popups[idx].map { if (isUpper) it.uppercase() else it }
            )
        }
        rows.add(row1)

        // Row 2: a s d f g h j k l with symbol hints
        val row2Letters = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row2Symbols = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")")
        val row2Popups = listOf(
            listOf("a", "@", "á", "à", "â", "ä", "æ", "ã", "å"),
            listOf("s", "$", "ß", "§", "ś"),
            listOf("d", "#", "d", "ð"),
            listOf("f", "_", "f"),
            listOf("g", "&", "g"),
            listOf("h", "-", "h"),
            listOf("j", "+", "j"),
            listOf("k", "(", "k"),
            listOf("l", ")", "l", "£")
        )

        val row2 = row2Letters.mapIndexed { idx, letter ->
            val isUpper = isShifted || isCapsLocked
            val primary = if (isUpper) letter.uppercase() else letter
            val secondary = row2Symbols[idx]
            KeyModel(
                primaryLabel = primary,
                secondaryLabel = secondary,
                action = KeyAction.InsertText(primary),
                secondaryAction = KeyAction.InsertText(secondary),
                popupOptions = row2Popups[idx].map { if (isUpper) it.uppercase() else it }
            )
        }
        rows.add(row2)

        // Row 3: Shift, z x c v b n m, Backspace
        val row3Letters = listOf("z", "x", "c", "v", "b", "n", "m")
        val row3Symbols = listOf("*", "\"", "'", ":", ";", "!", "?")
        val row3Popups = listOf(
            listOf("z", "*", "ž", "ź", "ż"),
            listOf("x", "\"", "x"),
            listOf("c", "'", "ç", "ć", "č", "©"),
            listOf("v", ":", "v"),
            listOf("b", ";", "b"),
            listOf("n", "!", "ñ", "ń"),
            listOf("m", "?", "m")
        )

        val row3Keys = mutableListOf<KeyModel>()

        // Shift key (Gboard style: ⇧ / ⇪)
        val shiftLabel = if (isCapsLocked) "⇪" else "⇧"
        row3Keys.add(
            KeyModel(
                primaryLabel = shiftLabel,
                action = KeyAction.ToggleShift,
                weight = 1.35f,
                isModifier = true,
                isSpecial = true
            )
        )

        row3Letters.forEachIndexed { idx, letter ->
            val isUpper = isShifted || isCapsLocked
            val primary = if (isUpper) letter.uppercase() else letter
            val secondary = row3Symbols[idx]
            row3Keys.add(
                KeyModel(
                    primaryLabel = primary,
                    secondaryLabel = secondary,
                    action = KeyAction.InsertText(primary),
                    secondaryAction = KeyAction.InsertText(secondary),
                    popupOptions = row3Popups[idx].map { if (isUpper) it.uppercase() else it }
                )
            )
        }

        // Backspace key
        row3Keys.add(
            KeyModel(
                primaryLabel = "⌫",
                action = KeyAction.Backspace,
                weight = 1.35f,
                isSpecial = true
            )
        )
        rows.add(row3Keys)

        // Row 4: ?123, Comma, Spacebar (Long-press to switch keyboard), Period, Enter ↵
        val row4 = listOf(
            KeyModel(
                primaryLabel = "?123",
                action = KeyAction.SwitchLayout(KeyboardLayoutMode.SYMBOLS_NUMBERS),
                weight = 1.4f,
                isSpecial = true
            ),
            KeyModel(
                primaryLabel = ",",
                secondaryLabel = "<",
                action = KeyAction.InsertText(","),
                secondaryAction = KeyAction.InsertText("<"),
                weight = 1.1f,
                popupOptions = listOf(",", "<", ";", "{", "(", "[", "\\")
            ),
            KeyModel(
                primaryLabel = "English",
                secondaryLabel = "🌐",
                action = KeyAction.Space,
                secondaryAction = KeyAction.SwitchToImePicker,
                weight = 4.4f
            ),
            KeyModel(
                primaryLabel = ".",
                secondaryLabel = ">",
                action = KeyAction.InsertText("."),
                secondaryAction = KeyAction.InsertText(">"),
                weight = 1.1f,
                popupOptions = listOf(".", ">", "/", "@", "?", "!", "-", "_", ":", ".com", ".org", ".io")
            ),
            KeyModel(
                primaryLabel = "↵",
                action = KeyAction.Enter,
                weight = 1.5f,
                isSpecial = true
            )
        )
        rows.add(row4)

        return rows
    }

    fun getGboardSymbols123(showNumberRow: Boolean): List<List<KeyModel>> {
        val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { num ->
            KeyModel(
                primaryLabel = num,
                action = KeyAction.InsertText(num),
                weight = 1.0f,
                popupOptions = when (num) {
                    "1" -> listOf("1", "¹", "½", "⅓", "¼")
                    "2" -> listOf("2", "²", "⅔")
                    "3" -> listOf("3", "³", "¾")
                    "4" -> listOf("4", "⁴")
                    "0" -> listOf("0", "⁰", "°", "∅")
                    else -> listOf(num)
                }
            )
        }

        val row2Symbols = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/")
        val row2 = row2Symbols.map { sym ->
            KeyModel(
                primaryLabel = sym,
                action = KeyAction.InsertText(sym),
                weight = 1.0f,
                popupOptions = when (sym) {
                    "$" -> listOf("$", "€", "£", "¥", "₹", "¢", "₽")
                    "%" -> listOf("%", "‰")
                    "&" -> listOf("&", "§")
                    "+" -> listOf("+", "±")
                    else -> listOf(sym)
                }
            )
        }

        val row3Keys = mutableListOf<KeyModel>()
        // Switch to extended symbols: =\<
        row3Keys.add(
            KeyModel(
                primaryLabel = "=\\<",
                action = KeyAction.SwitchLayout(KeyboardLayoutMode.SYMBOLS_EXTENDED),
                weight = 1.4f,
                isSpecial = true
            )
        )

        listOf("*", "\"", "'", ":", ";", "!", "?").forEach { sym ->
            row3Keys.add(
                KeyModel(
                    primaryLabel = sym,
                    action = KeyAction.InsertText(sym),
                    weight = 1.0f,
                    popupOptions = when (sym) {
                        "*" -> listOf("*", "★", "†", "‡")
                        "\"" -> listOf("\"", "“", "”", "«", "»")
                        "'" -> listOf("'", "‘", "’", "‚", "`")
                        "!" -> listOf("!", "¡")
                        "?" -> listOf("?", "¿")
                        else -> listOf(sym)
                    }
                )
            )
        }

        // Backspace
        row3Keys.add(
            KeyModel(
                primaryLabel = "⌫",
                action = KeyAction.Backspace,
                weight = 1.4f,
                isSpecial = true
            )
        )

        // Row 4: ABC, ,, Space (Hold to switch keyboard), ., Enter
        val row4 = listOf(
            KeyModel(
                primaryLabel = "ABC",
                action = KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY),
                weight = 1.5f,
                isSpecial = true
            ),
            KeyModel(
                primaryLabel = ",",
                action = KeyAction.InsertText(","),
                weight = 1.1f
            ),
            KeyModel(
                primaryLabel = "English",
                secondaryLabel = "🌐",
                action = KeyAction.Space,
                secondaryAction = KeyAction.SwitchToImePicker,
                weight = 4.4f
            ),
            KeyModel(
                primaryLabel = ".",
                action = KeyAction.InsertText("."),
                weight = 1.1f
            ),
            KeyModel(
                primaryLabel = "↵",
                action = KeyAction.Enter,
                weight = 1.5f,
                isSpecial = true
            )
        )

        return listOf(row1, row2, row3Keys, row4)
    }

    fun getGboardExtendedSymbols(): List<List<KeyModel>> {
        val row1Symbols = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
        val row1 = row1Symbols.map { sym ->
            KeyModel(primaryLabel = sym, action = KeyAction.InsertText(sym), weight = 1.0f)
        }

        val row2Symbols = listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\")
        val row2 = row2Symbols.map { sym ->
            KeyModel(primaryLabel = sym, action = KeyAction.InsertText(sym), weight = 1.0f)
        }

        val row3Keys = mutableListOf<KeyModel>()
        row3Keys.add(
            KeyModel(
                primaryLabel = "?123",
                action = KeyAction.SwitchLayout(KeyboardLayoutMode.SYMBOLS_NUMBERS),
                weight = 1.4f,
                isSpecial = true
            )
        )

        listOf("%", "©", "®", "™", "✓", "[", "]", "<", ">").forEach { sym ->
            row3Keys.add(
                KeyModel(primaryLabel = sym, action = KeyAction.InsertText(sym), weight = 1.0f)
            )
        }

        row3Keys.add(
            KeyModel(
                primaryLabel = "⌫",
                action = KeyAction.Backspace,
                weight = 1.4f,
                isSpecial = true
            )
        )

        val row4 = listOf(
            KeyModel(
                primaryLabel = "ABC",
                action = KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY),
                weight = 1.5f,
                isSpecial = true
            ),
            KeyModel(
                primaryLabel = "_",
                action = KeyAction.InsertText("_"),
                weight = 1.1f
            ),
            KeyModel(
                primaryLabel = "English",
                secondaryLabel = "🌐",
                action = KeyAction.Space,
                secondaryAction = KeyAction.SwitchToImePicker,
                weight = 4.4f
            ),
            KeyModel(
                primaryLabel = "…",
                action = KeyAction.InsertText("…"),
                weight = 1.1f
            ),
            KeyModel(
                primaryLabel = "↵",
                action = KeyAction.Enter,
                weight = 1.5f,
                isSpecial = true
            )
        )

        return listOf(row1, row2, row3Keys, row4)
    }

    fun getNumpadLayout(): List<List<KeyModel>> {
        val row1 = listOf(
            KeyModel(primaryLabel = "7", action = KeyAction.InsertText("7"), weight = 1.0f),
            KeyModel(primaryLabel = "8", action = KeyAction.InsertText("8"), weight = 1.0f),
            KeyModel(primaryLabel = "9", action = KeyAction.InsertText("9"), weight = 1.0f),
            KeyModel(primaryLabel = "/", action = KeyAction.InsertText(" / "), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "⌫", action = KeyAction.Backspace, weight = 1.2f, isSpecial = true)
        )

        val row2 = listOf(
            KeyModel(primaryLabel = "4", action = KeyAction.InsertText("4"), weight = 1.0f),
            KeyModel(primaryLabel = "5", action = KeyAction.InsertText("5"), weight = 1.0f),
            KeyModel(primaryLabel = "6", action = KeyAction.InsertText("6"), weight = 1.0f),
            KeyModel(primaryLabel = "*", action = KeyAction.InsertText(" * "), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "%", action = KeyAction.InsertText(" % "), weight = 1.2f, isSpecial = true)
        )

        val row3 = listOf(
            KeyModel(primaryLabel = "1", action = KeyAction.InsertText("1"), weight = 1.0f),
            KeyModel(primaryLabel = "2", action = KeyAction.InsertText("2"), weight = 1.0f),
            KeyModel(primaryLabel = "3", action = KeyAction.InsertText("3"), weight = 1.0f),
            KeyModel(primaryLabel = "-", action = KeyAction.InsertText(" - "), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "+", action = KeyAction.InsertText(" + "), weight = 1.2f, isSpecial = true)
        )

        val row4 = listOf(
            KeyModel(primaryLabel = "ABC", action = KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY), weight = 1.2f, isSpecial = true),
            KeyModel(primaryLabel = "0", action = KeyAction.InsertText("0"), weight = 1.0f),
            KeyModel(primaryLabel = ".", action = KeyAction.InsertText("."), weight = 1.0f),
            KeyModel(primaryLabel = "=", action = KeyAction.InsertText(" = "), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "↵", action = KeyAction.Enter, weight = 1.2f, isSpecial = true)
        )

        return listOf(row1, row2, row3, row4)
    }

    fun getFull5RowLayout(isShifted: Boolean, isCtrlActive: Boolean, isAltActive: Boolean, isFnActive: Boolean): List<List<KeyModel>> {
        val row0 = if (isFnActive) {
            listOf(
                KeyModel(primaryLabel = "Esc", action = KeyAction.Escape, weight = 1.0f, isSpecial = true),
                KeyModel(primaryLabel = "F1", action = KeyAction.InsertText("F1")),
                KeyModel(primaryLabel = "F2", action = KeyAction.InsertText("F2")),
                KeyModel(primaryLabel = "F3", action = KeyAction.InsertText("F3")),
                KeyModel(primaryLabel = "F4", action = KeyAction.InsertText("F4")),
                KeyModel(primaryLabel = "F5", action = KeyAction.InsertText("F5")),
                KeyModel(primaryLabel = "F6", action = KeyAction.InsertText("F6")),
                KeyModel(primaryLabel = "F7", action = KeyAction.InsertText("F7")),
                KeyModel(primaryLabel = "F8", action = KeyAction.InsertText("F8")),
                KeyModel(primaryLabel = "F9", action = KeyAction.InsertText("F9")),
                KeyModel(primaryLabel = "F10", action = KeyAction.InsertText("F10")),
                KeyModel(primaryLabel = "F11", action = KeyAction.InsertText("F11")),
                KeyModel(primaryLabel = "F12", action = KeyAction.InsertText("F12")),
                KeyModel(primaryLabel = "Del", action = KeyAction.DeleteForward, weight = 1.0f, isSpecial = true)
            )
        } else {
            val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=")
            val shiftedDigits = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+")
            val list = mutableListOf<KeyModel>()
            list.add(KeyModel(primaryLabel = "Esc", action = KeyAction.Escape, weight = 1.0f, isSpecial = true))
            digits.forEachIndexed { idx, d ->
                val primary = if (isShifted) shiftedDigits[idx] else d
                val secondary = if (isShifted) d else shiftedDigits[idx]
                list.add(
                    KeyModel(
                        primaryLabel = primary,
                        secondaryLabel = secondary,
                        action = KeyAction.InsertText(primary),
                        secondaryAction = KeyAction.InsertText(secondary)
                    )
                )
            }
            list.add(KeyModel(primaryLabel = "⌫", action = KeyAction.Backspace, weight = 1.2f, isSpecial = true))
            list
        }

        // Row 1: Tab, QWERTY..., [, ], \
        val row1Letters = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        val row1 = mutableListOf<KeyModel>()
        row1.add(KeyModel(primaryLabel = "Tab ⇥", action = KeyAction.Tab, weight = 1.2f, isSpecial = true))
        row1Letters.forEach { l ->
            val label = if (isShifted) l.uppercase() else l
            row1.add(KeyModel(primaryLabel = label, action = KeyAction.InsertText(label)))
        }
        val b1 = if (isShifted) "{" else "["
        val b2 = if (isShifted) "}" else "]"
        val slash = if (isShifted) "|" else "\\"
        row1.add(KeyModel(primaryLabel = b1, action = KeyAction.InsertText(b1), secondaryAction = KeyAction.InsertText(if (isShifted) "[" else "{")))
        row1.add(KeyModel(primaryLabel = b2, action = KeyAction.InsertText(b2), secondaryAction = KeyAction.InsertText(if (isShifted) "]" else "}")))
        row1.add(KeyModel(primaryLabel = slash, action = KeyAction.InsertText(slash), secondaryAction = KeyAction.InsertText(if (isShifted) "\\" else "|")))

        // Row 2: Ctrl, ASDF..., ;, ', Enter
        val row2Letters = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        val row2 = mutableListOf<KeyModel>()
        row2.add(KeyModel(primaryLabel = "Ctrl", action = KeyAction.ToggleCtrl, weight = 1.2f, isModifier = true))
        row2Letters.forEach { l ->
            val label = if (isShifted) l.uppercase() else l
            row2.add(KeyModel(primaryLabel = label, action = KeyAction.InsertText(label)))
        }
        val semi = if (isShifted) ":" else ";"
        val quote = if (isShifted) "\"" else "'"
        row2.add(KeyModel(primaryLabel = semi, action = KeyAction.InsertText(semi)))
        row2.add(KeyModel(primaryLabel = quote, action = KeyAction.InsertText(quote)))
        row2.add(KeyModel(primaryLabel = "Enter ↵", action = KeyAction.Enter, weight = 1.4f, isSpecial = true))

        // Row 3: Shift, ZXCV..., ,, ., /, Up, Del
        val row3Letters = listOf("z", "x", "c", "v", "b", "n", "m")
        val row3 = mutableListOf<KeyModel>()
        row3.add(KeyModel(primaryLabel = "Shift ⇧", action = KeyAction.ToggleShift, weight = 1.4f, isModifier = true))
        row3Letters.forEach { l ->
            val label = if (isShifted) l.uppercase() else l
            row3.add(KeyModel(primaryLabel = label, action = KeyAction.InsertText(label)))
        }
        val comma = if (isShifted) "<" else ","
        val dot = if (isShifted) ">" else "."
        val fwdSlash = if (isShifted) "?" else "/"
        row3.add(KeyModel(primaryLabel = comma, action = KeyAction.InsertText(comma)))
        row3.add(KeyModel(primaryLabel = dot, action = KeyAction.InsertText(dot)))
        row3.add(KeyModel(primaryLabel = fwdSlash, action = KeyAction.InsertText(fwdSlash)))
        row3.add(KeyModel(primaryLabel = "↑", action = KeyAction.CursorMove(0, -1), weight = 1.0f, isSpecial = true))
        row3.add(KeyModel(primaryLabel = "PgUp", action = KeyAction.PageUp, weight = 1.0f, isSpecial = true))

        // Row 4: Fn, Alt, SYM, Space, Left, Down, Right, Home, End
        val row4 = listOf(
            KeyModel(primaryLabel = "Fn", action = KeyAction.ToggleFn, weight = 1.0f, isModifier = true),
            KeyModel(primaryLabel = "Alt", action = KeyAction.ToggleAlt, weight = 1.0f, isModifier = true),
            KeyModel(primaryLabel = "SYM", action = KeyAction.SwitchLayout(KeyboardLayoutMode.SYMBOLS), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "␣ Space", action = KeyAction.Space, weight = 3.5f),
            KeyModel(primaryLabel = "←", action = KeyAction.CursorMove(-1, 0), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "↓", action = KeyAction.CursorMove(0, 1), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "→", action = KeyAction.CursorMove(1, 0), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "Home", action = KeyAction.CursorHome, weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "End", action = KeyAction.CursorEnd, weight = 1.0f, isSpecial = true)
        )

        return listOf(row0, row1, row2, row3, row4)
    }

    fun getSymbolsLayout(): List<List<KeyModel>> {
        val row0 = listOf(
            KeyModel(primaryLabel = "()", action = KeyAction.InsertText("()", cursorOffset = -1)),
            KeyModel(primaryLabel = "{}", action = KeyAction.InsertText("{}", cursorOffset = -1)),
            KeyModel(primaryLabel = "[]", action = KeyAction.InsertText("[]", cursorOffset = -1)),
            KeyModel(primaryLabel = "<>", action = KeyAction.InsertText("<>", cursorOffset = -1)),
            KeyModel(primaryLabel = "\"\"", action = KeyAction.InsertText("\"\"", cursorOffset = -1)),
            KeyModel(primaryLabel = "''", action = KeyAction.InsertText("''", cursorOffset = -1)),
            KeyModel(primaryLabel = "``", action = KeyAction.InsertText("``", cursorOffset = -1)),
            KeyModel(primaryLabel = "\${}", action = KeyAction.InsertText("\${}", cursorOffset = -1)),
            KeyModel(primaryLabel = "<!-- -->", action = KeyAction.InsertText("<!--  -->", cursorOffset = -4))
        )

        val row1 = listOf(
            KeyModel(primaryLabel = "==", action = KeyAction.InsertText(" == ")),
            KeyModel(primaryLabel = "!=", action = KeyAction.InsertText(" != ")),
            KeyModel(primaryLabel = "<=", action = KeyAction.InsertText(" <= ")),
            KeyModel(primaryLabel = ">=", action = KeyAction.InsertText(" >= ")),
            KeyModel(primaryLabel = "&&", action = KeyAction.InsertText(" && ")),
            KeyModel(primaryLabel = "||", action = KeyAction.InsertText(" || ")),
            KeyModel(primaryLabel = "->", action = KeyAction.InsertText("->")),
            KeyModel(primaryLabel = "=>", action = KeyAction.InsertText(" => ")),
            KeyModel(primaryLabel = "::", action = KeyAction.InsertText("::")),
            KeyModel(primaryLabel = ":=", action = KeyAction.InsertText(" := "))
        )

        val row2 = listOf(
            KeyModel(primaryLabel = "++", action = KeyAction.InsertText("++")),
            KeyModel(primaryLabel = "--", action = KeyAction.InsertText("--")),
            KeyModel(primaryLabel = "+=", action = KeyAction.InsertText(" += ")),
            KeyModel(primaryLabel = "-=", action = KeyAction.InsertText(" -= ")),
            KeyModel(primaryLabel = "*=", action = KeyAction.InsertText(" *= ")),
            KeyModel(primaryLabel = "/=", action = KeyAction.InsertText(" /= ")),
            KeyModel(primaryLabel = "%=", action = KeyAction.InsertText(" %= ")),
            KeyModel(primaryLabel = "??", action = KeyAction.InsertText(" ?? ")),
            KeyModel(primaryLabel = "?.", action = KeyAction.InsertText("?."))
        )

        val row3 = listOf(
            KeyModel(primaryLabel = "~", action = KeyAction.InsertText("~")),
            KeyModel(primaryLabel = "^", action = KeyAction.InsertText("^")),
            KeyModel(primaryLabel = "&", action = KeyAction.InsertText("&")),
            KeyModel(primaryLabel = "|", action = KeyAction.InsertText("|")),
            KeyModel(primaryLabel = "\\", action = KeyAction.InsertText("\\")),
            KeyModel(primaryLabel = "@", action = KeyAction.InsertText("@")),
            KeyModel(primaryLabel = "#", action = KeyAction.InsertText("#")),
            KeyModel(primaryLabel = "$", action = KeyAction.InsertText("$")),
            KeyModel(primaryLabel = "%", action = KeyAction.InsertText("%")),
            KeyModel(primaryLabel = "⌫", action = KeyAction.Backspace, weight = 1.2f, isSpecial = true)
        )

        val row4 = listOf(
            KeyModel(primaryLabel = "ABC", action = KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY), weight = 1.4f, isSpecial = true),
            KeyModel(primaryLabel = "Regex", action = KeyAction.InsertText(".*"), secondaryAction = KeyAction.InsertText("\\d+")),
            KeyModel(primaryLabel = ";", action = KeyAction.InsertText(";")),
            KeyModel(primaryLabel = "␣ Space", action = KeyAction.Space, weight = 3.0f),
            KeyModel(primaryLabel = ":", action = KeyAction.InsertText(":")),
            KeyModel(primaryLabel = "_", action = KeyAction.InsertText("_")),
            KeyModel(primaryLabel = "↵", action = KeyAction.Enter, weight = 1.4f, isSpecial = true)
        )

        return listOf(row0, row1, row2, row3, row4)
    }

    fun getTerminalLayout(): List<List<KeyModel>> {
        val row0 = listOf(
            KeyModel(primaryLabel = "ls -la", action = KeyAction.InsertText("ls -la\n"), isSpecial = true),
            KeyModel(primaryLabel = "cd ..", action = KeyAction.InsertText("cd ..\n"), isSpecial = true),
            KeyModel(primaryLabel = "pwd", action = KeyAction.InsertText("pwd\n"), isSpecial = true),
            KeyModel(primaryLabel = "clear", action = KeyAction.InsertText("clear\n"), isSpecial = true),
            KeyModel(primaryLabel = "git status", action = KeyAction.InsertText("git status\n"), isSpecial = true),
            KeyModel(primaryLabel = "git add .", action = KeyAction.InsertText("git add .\n"), isSpecial = true)
        )

        val row1 = listOf(
            KeyModel(primaryLabel = "sudo", action = KeyAction.InsertText("sudo ")),
            KeyModel(primaryLabel = "python3", action = KeyAction.InsertText("python3 ")),
            KeyModel(primaryLabel = "npm start", action = KeyAction.InsertText("npm start\n")),
            KeyModel(primaryLabel = "grep -rn", action = KeyAction.InsertText("grep -rn \"\" .", cursorOffset = -3)),
            KeyModel(primaryLabel = "curl -s", action = KeyAction.InsertText("curl -s ")),
            KeyModel(primaryLabel = "chmod +x", action = KeyAction.InsertText("chmod +x "))
        )

        val row2 = listOf(
            KeyModel(primaryLabel = "Ctrl+C", action = KeyAction.InsertText("^C\n"), isModifier = true),
            KeyModel(primaryLabel = "Ctrl+Z", action = KeyAction.InsertText("^Z\n"), isModifier = true),
            KeyModel(primaryLabel = "Ctrl+D", action = KeyAction.InsertText("^D\n"), isModifier = true),
            KeyModel(primaryLabel = "Ctrl+L", action = KeyAction.InsertText("^L\n"), isModifier = true),
            KeyModel(primaryLabel = "Esc", action = KeyAction.Escape, isSpecial = true),
            KeyModel(primaryLabel = ":wq", action = KeyAction.InsertText(":wq\n"), isSpecial = true)
        )

        val row3 = listOf(
            KeyModel(primaryLabel = "|", action = KeyAction.InsertText(" | ")),
            KeyModel(primaryLabel = ">", action = KeyAction.InsertText(" > ")),
            KeyModel(primaryLabel = ">>", action = KeyAction.InsertText(" >> ")),
            KeyModel(primaryLabel = "2>&1", action = KeyAction.InsertText(" 2>&1 ")),
            KeyModel(primaryLabel = "$()", action = KeyAction.InsertText("$()", cursorOffset = -1)),
            KeyModel(primaryLabel = "~", action = KeyAction.InsertText("~")),
            KeyModel(primaryLabel = "/", action = KeyAction.InsertText("/")),
            KeyModel(primaryLabel = "⌫", action = KeyAction.Backspace, weight = 1.2f, isSpecial = true)
        )

        val row4 = listOf(
            KeyModel(primaryLabel = "ABC", action = KeyAction.SwitchLayout(KeyboardLayoutMode.QWERTY), weight = 1.4f, isSpecial = true),
            KeyModel(primaryLabel = "SYM", action = KeyAction.SwitchLayout(KeyboardLayoutMode.SYMBOLS), weight = 1.0f, isSpecial = true),
            KeyModel(primaryLabel = "␣ Space", action = KeyAction.Space, weight = 3.5f),
            KeyModel(primaryLabel = "Tab ⇥", action = KeyAction.Tab, weight = 1.1f, isSpecial = true),
            KeyModel(primaryLabel = "↵ Exec", action = KeyAction.Enter, weight = 1.4f, isSpecial = true)
        )

        return listOf(row0, row1, row2, row3, row4)
    }

    // Backwards compatibility
    fun getCompactQwerty(isShifted: Boolean, isCtrlActive: Boolean): List<List<KeyModel>> {
        return getGboardQwerty(isShifted = isShifted, isCapsLocked = false, showNumberRow = false)
    }

    fun getNumPadHexLayout(): List<List<KeyModel>> {
        return getNumpadLayout()
    }
}
