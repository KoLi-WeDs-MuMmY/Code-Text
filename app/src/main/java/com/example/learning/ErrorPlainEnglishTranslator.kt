package com.example.learning

import com.example.model.LanguageType

data class FriendlyErrorExplanation(
    val headline: String,
    val simpleMeaning: String,
    val realLifeTip: String,
    val howToFix: String
)

/**
 * Translates technical compiler/syntax error messages into beginner-friendly plain English
 * with real-life analogies and maintains a tap-to-define coding word dictionary.
 */
object ErrorPlainEnglishTranslator {

    fun translate(diagnosticMessage: String, language: LanguageType): FriendlyErrorExplanation {
        val lower = diagnosticMessage.lowercase()
        return when {
            lower.contains("unclosed '{'") || lower.contains("missing") && lower.contains("'}'") ->
                FriendlyErrorExplanation(
                    headline = "Open Box Missing Its Lid `}`",
                    simpleMeaning = "You started a code group with `{`, like opening a cardboard box, but forgot to put the closing lid `}` at the end.",
                    realLifeTip = "Every `{` door needs a matching `}` door so the computer knows where that block ends.",
                    howToFix = "Add a closing `}` on a new line at the end of your function or loop."
                )
            lower.contains("unclosed '('") || lower.contains("expecting ')'") || lower.contains("')'") ->
                FriendlyErrorExplanation(
                    headline = "Missing Closing Parenthesis `)`",
                    simpleMeaning = "You opened `(` to pass information into a command, but didn't close it with `)`.",
                    realLifeTip = "Think of `(` and `)` like the left and right bookends holding values together.",
                    howToFix = "Check the end of the line and add the missing `)`."
                )
            lower.contains("unclosed string literal") || lower.contains("unclosed quote") ->
                FriendlyErrorExplanation(
                    headline = "Sentence Quote Was Left Open",
                    simpleMeaning = "You started typing text with `\"` (or `'`), so the computer is still reading the rest of your file as part of that sentence.",
                    realLifeTip = "Quotes act like speech bubbles: you need one quote when someone starts speaking and another when they stop.",
                    howToFix = "Put a matching `\"` right after your message ends."
                )
            lower.contains("semicolon") || lower.contains("missing ';'") ->
                FriendlyErrorExplanation(
                    headline = "Missing Period `;` at End of Line",
                    simpleMeaning = "${language.displayName} uses `;` the same way English uses a period (`.`) to mark the end of one instruction.",
                    realLifeTip = "Without `;`, two separate instructions accidentally glue together into one confusing sentence.",
                    howToFix = "Type `;` at the very end of the flagged line."
                )
            lower.contains("'fun'") || lower.contains("function") ->
                FriendlyErrorExplanation(
                    headline = "Keyword Should Be `fun`",
                    simpleMeaning = "Kotlin uses the short word `fun` (short for function) instead of `function` to create a reusable recipe.",
                    realLifeTip = "Like saying 'Recipe:' instead of writing out a full paragraph header.",
                    howToFix = "Replace `function` with `fun`."
                )
            lower.contains("indentation") || lower.contains("indent") ->
                FriendlyErrorExplanation(
                    headline = "Indentation (Spacing) Is Off",
                    simpleMeaning = "Python groups steps together by how far right they are pushed with spaces (usually 4 spaces).",
                    realLifeTip = "Just like bullet sub-items tucked slightly to the right under a heading in your notebook.",
                    howToFix = "Press Tab (4 spaces) cleanly on lines inside `if`, `for`, or `def`."
                )
            lower.contains("unresolved") || lower.contains("import") ->
                FriendlyErrorExplanation(
                    headline = "Missing Helper Tool (`import`)",
                    simpleMeaning = "Your code uses a helper class that lives in a library toolbox not yet brought onto the workbench.",
                    realLifeTip = "Before baking with a mixer, you have to take it out of the kitchen cabinet (`import`).",
                    howToFix = "Tap the `+ Import` quick-fix button to add the import line at the top automatically."
                )
            lower.contains("unclosed xml") || lower.contains("closing tag") || lower.contains("</") ->
                FriendlyErrorExplanation(
                    headline = "XML Tag Needs a Closing Tag",
                    simpleMeaning = "In Android XML, opening `<LinearLayout>` or `<TextView>` tags must close with either `/>` or `</LinearLayout>`.",
                    realLifeTip = "Think of HTML/XML tags like nested lunchbox lids: every container opened must be sealed.",
                    howToFix = "End self-contained widgets like `<TextView ... />` with `/>`, and container tags with `</TagName>`."
                )
            else ->
                FriendlyErrorExplanation(
                    headline = "Syntax Check Notice",
                    simpleMeaning = "The ${language.displayName} checker spotted something unexpected on this line: $diagnosticMessage",
                    realLifeTip = "Check spelling, matching brackets `() {}`, and quotes `\"\"` right around this line.",
                    howToFix = "Tap a 💡 Quick-Fix suggestion if shown, or compare carefully with the Lesson Solution."
                )
        }
    }

    val globalGlossary: List<WordMeaning> = listOf(
        WordMeaning(
            term = "fun / def / fn",
            partOfSpeech = "Keyword (Action)",
            simpleMeaning = "Defines a reusable mini-program (a function) that runs whenever you call its name.",
            everydayExample = "Like saving a 'Make Toast' button on a toaster instead of explaining how to heat wires each morning."
        ),
        WordMeaning(
            term = "val vs var",
            partOfSpeech = "Storage Label",
            simpleMeaning = "`val` is a locked box whose label cannot be reassigned; `var` is an open jar whose number or text can change later.",
            everydayExample = "Your birthday is a `val` (fixed); your game score is a `var` (keeps increasing)."
        ),
        WordMeaning(
            term = "for / range",
            partOfSpeech = "Loop",
            simpleMeaning = "Repeats the same instructions once for each item or number in a list.",
            everydayExample = "Like stamping every envelope in a stack from #1 through #5 before going to lunch."
        ),
        WordMeaning(
            term = "println / print",
            partOfSpeech = "Output Command",
            simpleMeaning = "Shows a message on the screen (console) so you can see what the program calculated.",
            everydayExample = "Like a calculator screen lighting up to display the final answer."
        ),
        WordMeaning(
            term = "@Composable / Widget",
            partOfSpeech = "UI Building Block",
            simpleMeaning = "Describes a visual piece of screen UI (a card, text label, or button) that automatically updates when data changes.",
            everydayExample = "Like snapping LEGO UI bricks (`Card`, `Column`, `Text`) into a phone frame."
        ),
        WordMeaning(
            term = "Scaffold / LinearLayout",
            partOfSpeech = "Screen Skeleton",
            simpleMeaning = "A layout frame that holds top bars, buttons, and cards neatly stacked vertically or side-by-side.",
            everydayExample = "Like a bento lunchbox with separated compartments so food never piles into a mess."
        ),
        WordMeaning(
            term = "@string/...",
            partOfSpeech = "Android Resource",
            simpleMeaning = "Looks up text stored in `strings.xml` instead of hardcoding words inside layouts, making translations easy.",
            everydayExample = "Like pointing to an item number on a restaurant menu instead of rewriting the recipe each time."
        )
    )
}
