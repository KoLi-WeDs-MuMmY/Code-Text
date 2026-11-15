package com.example.learning

import com.example.model.LanguageType

object StudentCurriculumCatalog {

    val lessons: List<CodingLesson> = listOf(
        CodingLesson(
            id = "kt_loops_stack",
            track = LearningTrack.KOTLIN_ANDROID,
            title = "Loops, Ranges & Stack Sum",
            conceptTag = "Loops & KtVM Stack",
            difficulty = "Beginner",
            language = LanguageType.KOTLIN,
            instructions = "Write a Kotlin `main()` function that adds the numbers `1 + 2 + 3 + 4 + 5` into `total`, then prints `Total=15`.",
            hint = "Write `for (i in 1..5) { total += i }` right after `var total = 0`.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "The Piggy Bank & 5 Coins Story",
                story = "Imagine placing an empty piggy bank labeled `total` on your desk (`0` coins inside). Now you pick up coins numbered 1, 2, 3, 4, and 5 one by one and drop each into the piggy bank.",
                mappingSummary = "`var total = 0` is the empty jar; `for (i in 1..5)` picks each coin; `total += i` drops that coin inside."
            ),
            wordDictionary = listOf(
                WordMeaning("fun", "Keyword", "Short for 'function' — a named recipe of steps the phone runs.", "Saying 'Morning Routine()' starts all steps inside."),
                WordMeaning("var", "Variable", "A changeable labeled storage box (its value can be updated).", "A scoreboard jar whose total keeps going up."),
                WordMeaning("for (i in 1..5)", "Loop", "Repeats the steps inside `{ }` while `i` counts 1, 2, 3, 4, 5.", "Inspecting seats #1 through #5 in a row."),
                WordMeaning("total += i", "Operator", "Shortcut for `total = total + i` — adds `i` onto what is already in `total`.", "Dropping ₹5 into a jar that already holds ₹10."),
                WordMeaning("println(...)", "Command", "Prints text to the terminal screen and starts a new line.", "Writing the final receipt on paper.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "fun main() {", "Doorway: Android starts running the program right here."),
                LineTranslation(2, "var total = 0", "Set up an empty piggy bank named `total` holding 0."),
                LineTranslation(3, "for (i in 1..5) { total += i }", "Count `i` from 1 to 5 and add each number into `total`."),
                LineTranslation(6, "println(\"Total=\" + total)", "Show the text `Total=15` on screen.")
            ),
            starterCode = """
                fun main() {
                    var total = 0
                    // TODO: loop 1..5 and add each i to total
                    println("Total=" + total)
                }
            """.trimIndent(),
            solutionCode = """
                fun main() {
                    var total = 0
                    for (i in 1..5) {
                        total += i
                    }
                    println("Total=" + total)
                }
            """.trimIndent(),
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.VariableEquals("total", "15", "Variable `total` equals 15"),
                LessonTestAssertion.ContainsStdout("Total=15", "Prints exact line Total=15")
            ),
            xpReward = 60
        ),
        CodingLesson(
            id = "kt_compose_ui_card",
            track = LearningTrack.KOTLIN_ANDROID,
            title = "Jetpack Compose Profile Card",
            conceptTag = "Compose Layout UI",
            difficulty = "Beginner",
            language = LanguageType.KOTLIN,
            instructions = "Build a Jetpack Compose `Card` containing a `Column`, a greeting `Text(\"Student Profile\")`, and an interactive `Button(\"Enroll\")`. Open the `UI Preview` tab to see it live!",
            hint = "Wrap `Text(...)` and `Button(onClick = {}) { Text(\"Enroll\") }` inside `Card { Column { ... } }`.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "Building a Student ID Card with LEGO Bricks",
                story = "Instead of painting pixels by hand, Jetpack Compose works like snapping labeled LEGO containers together: a plastic ID Card frame (`Card`) holds a vertical stack (`Column`) with your title, track label, and tap button.",
                mappingSummary = "`Card` = ID badge frame, `Column` = vertical stack top-to-bottom, `Text` = printed label, `Button` = clickable sticker."
            ),
            wordDictionary = listOf(
                WordMeaning("@Composable", "Annotation", "Marks a function as a UI blueprint that draws onto the screen.", "Like a 'Screen Blueprint' stamp on an architect's sheet."),
                WordMeaning("Card { ... }", "Container", "A rounded, elevated panel with a shadow and border.", "Like an index card resting slightly raised on a desk."),
                WordMeaning("Column { ... }", "Layout", "Places every child UI piece neatly one below another.", "Books stacked vertically on a shelf."),
                WordMeaning("Button(onClick = {})", "Widget", "A tappable button that runs code inside `onClick` when pressed.", "A doorbell: nothing happens until someone presses it.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "@Composable fun StudentCard()", "Declare a screen UI piece named `StudentCard`."),
                LineTranslation(3, "Card {", "Draw a rounded container card with subtle elevation."),
                LineTranslation(4, "Column {", "Stack items inside the card from top to bottom."),
                LineTranslation(7, "Button(onClick = {}) { Text(\"Enroll\") }", "Place a tappable button labeled `Enroll`.")
            ),
            starterCode = """
                @Composable
                fun StudentCard() {
                    Card {
                        Column {
                            Text("Student Profile")
                            Text("Track: Android Compose")
                            Button(onClick = {}) {
                                Text("Enroll")
                            }
                        }
                    }
                }

                fun main() {
                    println("ComposeTreeReady")
                }
            """.trimIndent(),
            solutionCode = """
                @Composable
                fun StudentCard() {
                    Card {
                        Column {
                            Text("Student Profile")
                            Text("Track: Android Compose")
                            Button(onClick = {}) {
                                Text("Enroll")
                            }
                        }
                    }
                }

                fun main() {
                    println("ComposeTreeReady")
                }
            """.trimIndent(),
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.ContainsStdout("ComposeTreeReady", "Compose preview module compiles cleanly")
            ),
            xpReward = 70,
            supportsUiPreview = true
        ),
        CodingLesson(
            id = "xml_android_layout",
            track = LearningTrack.KOTLIN_ANDROID,
            title = "Android XML Screen & Resources",
            conceptTag = "Android XML Layout",
            difficulty = "Beginner",
            language = LanguageType.XML,
            instructions = "Edit this Android XML layout file so it renders a `TextView` using `@string/welcome_title` and a `Button` labeled `Enroll`. Open `UI Preview` & `Android` tabs to inspect!",
            hint = "Make sure `<TextView android:text=\"@string/welcome_title\" />` and `<Button android:text=\"Enroll\" />` sit inside `<LinearLayout>`.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "A Bento Box with Menu Number Labels",
                story = "Classic Android XML designs screens like a bento lunchbox: `<LinearLayout>` is the tray divider stacking compartments vertically, while `@string/welcome_title` points to a clean menu list (`strings.xml`) so changing text or language is effortless.",
                mappingSummary = "`<LinearLayout>` = tray frame, `<TextView>` = text label, `<Button>` = action button, `@string/...` = shared text lookup."
            ),
            wordDictionary = listOf(
                WordMeaning("<LinearLayout>", "XML Tag", "Arranges Android views in a single vertical or horizontal line.", "Like train cars linked in one direction."),
                WordMeaning("android:orientation", "Attribute", "Chooses `vertical` (stacked) or `horizontal` (side by side).", "Turning a shelf upright vs sideways."),
                WordMeaning("<TextView>", "UI Tag", "Displays read-only text on an Android screen.", "A printed street sign."),
                WordMeaning("@string/welcome_title", "Resource Ref", "Reads the phrase from `strings.xml` instead of typing it directly.", "Referring to 'Item #2' on a printed menu.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "<LinearLayout android:orientation=\"vertical\">", "Create a vertical container holding views top-to-bottom."),
                LineTranslation(2, "<TextView android:text=\"@string/welcome_title\" />", "Display the greeting looked up from `strings.xml`."),
                LineTranslation(3, "<Button android:text=\"Enroll\" />", "Show a clickable Android button labeled `Enroll`."),
                LineTranslation(4, "</LinearLayout>", "Seal the layout container tag.")
            ),
            starterCode = """
                <LinearLayout android:orientation="vertical">
                    <TextView
                        android:text="@string/welcome_title"
                        android:textStyle="bold" />
                    <!-- TODO: add a Button with android:text="Enroll" -->
                </LinearLayout>
            """.trimIndent(),
            solutionCode = """
                <LinearLayout android:orientation="vertical">
                    <TextView
                        android:text="@string/welcome_title"
                        android:textStyle="bold" />
                    <Button
                        android:text="Enroll" />
                </LinearLayout>
            """.trimIndent(),
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.ContainsStdout("TextView", "Includes a TextView widget"),
                LessonTestAssertion.ContainsStdout("Button(Enroll)", "Includes a Button labeled Enroll")
            ),
            xpReward = 70,
            supportsUiPreview = true
        ),
        CodingLesson(
            id = "dart_flutter_widget",
            track = LearningTrack.FLUTTER_DART,
            title = "Flutter Widget Tree & Text",
            conceptTag = "Flutter Widget Tree",
            difficulty = "Beginner",
            language = LanguageType.DART,
            instructions = "Create a Flutter-style widget tree with `Scaffold`, `AppBar(title: Text('Study App'))`, and a `Column` showing `Text('Module 1')` plus an `ElevatedButton` labeled `'Start'`.",
            hint = "Open the `UI Preview` drawer tab as you type to see your Flutter `Scaffold` and `Column` update instantaneously.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "A Classroom Whiteboard Inside a Frame",
                story = "Think of Flutter's `Scaffold` as a classroom wall frame: the top wooden rail (`appBar`) holds the class title sign, and the open chalkboard area (`body`) pins your list (`Column`) of notes (`Text`) and interactive buttons (`ElevatedButton`).",
                mappingSummary = "`Scaffold` = full page skeleton, `AppBar` = top banner bar, `body: Column` = stacked page contents."
            ),
            wordDictionary = listOf(
                WordMeaning("Widget", "UI Class", "In Flutter, almost everything on screen—text, padding, buttons, layouts—is a Widget.", "Like tiles on a mosaic wall."),
                WordMeaning("Scaffold", "Page Frame", "Gives a screen standard Material structure: top `appBar` + main `body`.", "A picture frame with a title plaque at the top."),
                WordMeaning("children: [ ... ]", "List", "A bracketed list holding multiple widgets inside a Column or Row.", "Items placed inside a grocery basket."),
                WordMeaning("ElevatedButton", "Widget", "A raised button with shadow that lifts off the page visually.", "A physical push-button on a dashboard.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "Widget buildApp() {", "Define a function returning the screen's Widget tree."),
                LineTranslation(2, "return Scaffold(", "Build the full-page Material skeleton."),
                LineTranslation(3, "appBar: AppBar(title: Text('Study App')),", "Pin 'Study App' in the top header bar."),
                LineTranslation(4, "body: Column(children: [ ... ])", "Stack the module label and Start button vertically inside the page.")
            ),
            starterCode = """
                Widget buildApp() {
                  return Scaffold(
                    appBar: AppBar(title: Text('Study App')),
                    body: Column(
                      children: [
                        Text('Module 1'),
                        ElevatedButton(child: Text('Start')),
                      ],
                    ),
                  );
                }

                void main() {
                  print('FlutterReady');
                }
            """.trimIndent(),
            solutionCode = """
                Widget buildApp() {
                  return Scaffold(
                    appBar: AppBar(title: Text('Study App')),
                    body: Column(
                      children: [
                        Text('Module 1'),
                        ElevatedButton(child: Text('Start')),
                      ],
                    ),
                  );
                }

                void main() {
                  print('FlutterReady');
                }
            """.trimIndent(),
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.ContainsStdout("FlutterReady", "Dart code executes with FlutterReady output")
            ),
            xpReward = 65,
            supportsUiPreview = true
        ),
        CodingLesson(
            id = "py_even_sum",
            track = LearningTrack.PYTHON_DSA,
            title = "Python Even Filter & Stdin",
            conceptTag = "Python Loops & I/O",
            difficulty = "Intermediate",
            language = LanguageType.PYTHON,
            instructions = "Read an integer `limit = int(input())` (auto-grader supplies `8`), sum all even numbers strictly below `limit` (`2 + 4 + 6 = 12`) in `total`, and print `EvenSum: 12`.",
            hint = "Check `if i % 2 == 0:` inside `for i in range(1, limit):` and add `i` to `total`.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "Checking Ticket Numbers at the Gate",
                story = "Imagine standing at a concert gate with tickets numbered 1 through 7. Your rule (`i % 2 == 0`) asks: 'When divided by 2, is there zero leftover?' Only even-numbered tickets (2, 4, 6) get added to the VIP clipboard (`total`).",
                mappingSummary = "`input()` asks the user for a number; `range(1, limit)` hands out tickets 1..7; `i % 2 == 0` checks if `i` is even."
            ),
            wordDictionary = listOf(
                WordMeaning("input()", "Input Call", "Waits for the user (or Terminal `stdin >`) to type text.", "A ticket booth clerk asking 'What number?'"),
                WordMeaning("int(...)", "Converter", "Turns typed text like `\"8\"` into real math number `8`.", "Turning the word 'eight' into 8 apples you can add."),
                WordMeaning("range(1, limit)", "Sequence", "Generates numbers starting at 1 and stopping *before* `limit`.", "A ruler marked up to, but not including, the last tick."),
                WordMeaning("% (Modulo)", "Math Operator", "Gives the *remainder* after dividing two numbers (`6 % 2 == 0`, `5 % 2 == 1`).", "Splitting 5 cookies between 2 friends leaves 1 cookie leftover.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "limit = int(input())", "Read a typed number from `stdin` (8) and store it in `limit`."),
                LineTranslation(2, "total = 0", "Start our running sum at 0."),
                LineTranslation(3, "for i in range(1, limit):", "Check each number `i` from 1 up to 7."),
                LineTranslation(4, "if i % 2 == 0: total += i", "If `i` divides evenly by 2, add it to `total` (2 + 4 + 6 = 12).")
            ),
            starterCode = """
                limit = int(input())
                total = 0
                for i in range(1, limit):
                    # TODO: add even numbers to total
                    pass
                print("EvenSum:", total)
            """.trimIndent(),
            solutionCode = """
                limit = int(input())
                total = 0
                for i in range(1, limit):
                    if i % 2 == 0:
                        total += i
                print("EvenSum:", total)
            """.trimIndent(),
            stdinInput = "8",
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.VariableEquals("total", "12", "Variable `total` equals 12 for limit=8"),
                LessonTestAssertion.ContainsStdout("EvenSum: 12", "Stdout includes EvenSum: 12")
            ),
            xpReward = 75
        ),
        CodingLesson(
            id = "cpp_factorial_loop",
            track = LearningTrack.SYSTEMS_CPP_RUST,
            title = "C++ Factorial Accumulator",
            conceptTag = "C++ Loops & State",
            difficulty = "Intermediate",
            language = LanguageType.CPP,
            instructions = "Compute `5!` (`1 * 2 * 3 * 4 * 5 = 120`) using a `for` loop into variable `fact`, then print `Fact=120` via `std::cout`.",
            hint = "Start with `int fact = 1;` and loop `for (int i = 1; i <= 5; i++) { fact = fact * i; }`.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "Snowball Rolling Down a 5-Step Hill",
                story = "Multiplication starts with `1` (not `0`, because anything times 0 stays 0!). At each step down the hill (`i = 1, 2, 3, 4, 5`), your snowball grows by multiplying its current size by the step number: `1 -> 2 -> 6 -> 24 -> 120`.",
                mappingSummary = "`int fact = 1;` holds an integer number in fast stack memory; `std::cout <<` pushes output out like water through a pipe."
            ),
            wordDictionary = listOf(
                WordMeaning("#include <iostream>", "Preprocessor", "Loads C++'s standard screen-printing (`cout`) and keyboard tools.", "Plugging a microphone into the speaker before speaking."),
                WordMeaning("int", "Data Type", "Short for 'integer' — tells C++ to reserve a small memory box for whole numbers.", "Ordering a rigid egg-carton sized strictly for whole numbers."),
                WordMeaning("i++", "Increment", "Shortcut for `i = i + 1` (advance the counter by 1 after each lap).", "Clicking a hand-tally counter once per lap."),
                WordMeaning("std::cout <<", "Output Stream", "Sends values left-ward (`<<`) to the terminal display.", "A conveyor belt carrying text directly onto the screen.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "#include <iostream>", "Bring in C++ screen-printing tools."),
                LineTranslation(4, "int fact = 1;", "Create a whole-number variable `fact` starting at 1."),
                LineTranslation(5, "for (int i = 1; i <= 5; i++)", "Repeat 5 times while `i` steps 1, 2, 3, 4, 5."),
                LineTranslation(6, "fact = fact * i;", "Multiply `fact` by `i` each lap to reach 120.")
            ),
            starterCode = """
                #include <iostream>

                int main() {
                    int fact = 1;
                    for (int i = 1; i <= 5; i++) {
                        // TODO: multiply fact by i
                    }
                    std::cout << "Fact=" << fact << std::endl;
                    return 0;
                }
            """.trimIndent(),
            solutionCode = """
                #include <iostream>

                int main() {
                    int fact = 1;
                    for (int i = 1; i <= 5; i++) {
                        fact = fact * i;
                    }
                    std::cout << "Fact=" << fact << std::endl;
                    return 0;
                }
            """.trimIndent(),
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.VariableEquals("fact", "120", "Variable `fact` equals 120"),
                LessonTestAssertion.ContainsStdout("Fact=120", "Stdout includes Fact=120")
            ),
            xpReward = 80
        ),
        CodingLesson(
            id = "ai_classifier_rule",
            track = LearningTrack.AI_PROMPT_LOGIC,
            title = "On-Device Intent Classifier",
            conceptTag = "AI Logic & Classification",
            difficulty = "Intermediate",
            language = LanguageType.KOTLIN,
            instructions = "Write a `classifyTopic(prompt: String): String` function that returns `\"UI\"` if the prompt contains `\"button\"`, and test it on `\"Add a button\"` so it prints `Intent=UI`.",
            hint = "Use `if (prompt.contains(\"button\")) return \"UI\"` and print `\"Intent=\" + classifyTopic(\"Add a button\")`.",
            realLifeAnalogy = RealLifeAnalogy(
                headline = "A Smart Receptionist Routing Support Requests",
                story = "Think of an intent classifier like a school front-desk helper reading notes. Whenever a note mentions 'button' or 'screen', they route it straight to the Design (`UI`) desk; otherwise they place it in the `General` tray.",
                mappingSummary = "`prompt: String` = incoming user message, `if (prompt.contains(...))` = keyword detector, `return \"UI\"` = category label."
            ),
            wordDictionary = listOf(
                WordMeaning("String", "Data Type", "A chain of text characters inside quotes like `\"Add a button\"`.", "Beads of letters strung onto a necklace."),
                WordMeaning(".contains(...)", "Method", "Checks whether a smaller piece of text appears anywhere inside a sentence.", "Using Ctrl+F inside a document to spot a word."),
                WordMeaning("return", "Control Word", "Hands the answer back to whoever called the function and exits immediately.", "A chef passing the finished plate through the kitchen window."),
                WordMeaning("Intent", "AI Term", "The underlying goal behind what a user typed or asked.", "Hearing 'I'm cold' and recognizing the intent is 'Turn on heater'.")
            ),
            lineWalkthrough = listOf(
                LineTranslation(1, "fun classifyTopic(prompt: String): String {", "Accept text `prompt` and promise to hand back a category `String`."),
                LineTranslation(2, "if (prompt.contains(\"button\")) { return \"UI\" }", "If the message mentions `button`, label its intent as `UI`."),
                LineTranslation(5, "return \"General\"", "Otherwise, safely fall back to `General`."),
                LineTranslation(9, "val intent = classifyTopic(\"Add a button\")", "Test our classifier with a real sample message.")
            ),
            starterCode = """
                fun classifyTopic(prompt: String): String {
                    // TODO: return "UI" when prompt mentions "button"
                    return "General"
                }

                fun main() {
                    val intent = classifyTopic("Add a button")
                    println("Intent=" + intent)
                }
            """.trimIndent(),
            solutionCode = """
                fun classifyTopic(prompt: String): String {
                    if (prompt.contains("button")) {
                        return "UI"
                    }
                    return "General"
                }

                fun main() {
                    val intent = classifyTopic("Add a button")
                    println("Intent=" + intent)
                }
            """.trimIndent(),
            assertions = listOf(
                LessonTestAssertion.ExitCodeZero(),
                LessonTestAssertion.ContainsStdout("Intent=UI", "Classifier outputs Intent=UI")
            ),
            xpReward = 70
        )
    )

    val quizzes: List<ConceptQuizQuestion> = listOf(
        ConceptQuizQuestion(
            id = "q_kt_val_var",
            track = LearningTrack.KOTLIN_ANDROID,
            conceptTag = "Loops & KtVM Stack",
            question = "What does the Kotlin Stack VM print when this loop finishes?",
            codeSnippet = """
                var acc = 1
                for (i in 1..3) {
                    acc = acc * i
                }
                println(acc)
            """.trimIndent(),
            options = listOf("3", "6", "7", "9"),
            correctIndex = 1,
            explanation = "1 * 1 * 2 * 3 equals 6. Each iteration updates `acc` in the active stack frame."
        ),
        ConceptQuizQuestion(
            id = "q_compose_state",
            track = LearningTrack.KOTLIN_ANDROID,
            conceptTag = "Compose Layout UI",
            question = "Why do Compose UI components wrap mutable state in `remember { mutableStateOf(...) }`?",
            codeSnippet = "var count by remember { mutableStateOf(0) }",
            options = listOf(
                "To preserve state across recompositions",
                "To compile the app into C++",
                "To request storage permissions",
                "To disable dark mode"
            ),
            correctIndex = 0,
            explanation = "Without `remember`, a Composable function would reset `count` back to 0 every time it recomposes."
        ),
        ConceptQuizQuestion(
            id = "q_xml_resources",
            track = LearningTrack.KOTLIN_ANDROID,
            conceptTag = "Android XML Layout",
            question = "Why does Android XML use `android:text=\"@string/app_name\"` instead of hardcoding words?",
            codeSnippet = "<TextView android:text=\"@string/app_name\" />",
            options = listOf(
                "So text stays organized in strings.xml for reuse and translation",
                "Because XML forbids typing letters directly",
                "To speed up Wi-Fi downloads",
                "To grant camera permissions automatically"
            ),
            correctIndex = 0,
            explanation = "Pointing to `@string/...` lets you update labels or translate the entire app from `strings.xml` without hunting through layouts."
        ),
        ConceptQuizQuestion(
            id = "q_flutter_tree",
            track = LearningTrack.FLUTTER_DART,
            conceptTag = "Flutter Widget Tree",
            question = "In Flutter & Dart, which layout widget arranges children vertically from top to bottom?",
            codeSnippet = "body: _____(children: [Text('A'), Text('B')])",
            options = listOf("Row", "Column", "Stack", "AppBar"),
            correctIndex = 1,
            explanation = "`Column` lays out widgets vertically; `Row` lays them out horizontally; `Stack` overlaps them."
        ),
        ConceptQuizQuestion(
            id = "q_py_range",
            track = LearningTrack.PYTHON_DSA,
            conceptTag = "Python Loops & I/O",
            question = "Which values does `range(1, 5)` yield in Python?",
            codeSnippet = "for i in range(1, 5):\n    print(i)",
            options = listOf("1, 2, 3, 4, 5", "1, 2, 3, 4", "0, 1, 2, 3, 4", "2, 3, 4, 5"),
            correctIndex = 1,
            explanation = "Python's `range(start, stop)` includes `start` (1) and excludes `stop` (5), yielding 1, 2, 3, 4."
        ),
        ConceptQuizQuestion(
            id = "q_cpp_mem",
            track = LearningTrack.SYSTEMS_CPP_RUST,
            conceptTag = "C++ Loops & State",
            question = "Where are local variables inside a function call typically allocated during execution?",
            codeSnippet = "int step(int x) { int y = x + 2; return y; }",
            options = listOf("Call Stack Frame", "ROM Bootloader", "Network Socket", "Gradle Cache"),
            correctIndex = 0,
            explanation = "Function parameters and local primitives (`x`, `y`) live inside the active Call Stack Frame and are popped on `return`."
        ),
        ConceptQuizQuestion(
            id = "q_ai_socratic",
            track = LearningTrack.AI_PROMPT_LOGIC,
            conceptTag = "AI Logic & Classification",
            question = "Why is an offline AST fallback essential in an educational coding app?",
            codeSnippet = "if (networkUnavailable) useLocalAstTutor()",
            options = listOf(
                "Students still get <5ms hints offline with zero API outage",
                "It increases APK size to 300MB",
                "It requires root access",
                "It skips syntax validation"
            ),
            correctIndex = 0,
            explanation = "Deterministic local AST analysis keeps classroom practice fast and reliable even without cellular/Wi-Fi."
        )
    )

    fun lessonById(id: String?): CodingLesson? = lessons.find { it.id == id }
    fun quizzesForTrack(track: LearningTrack?): List<ConceptQuizQuestion> =
        if (track == null) quizzes else quizzes.filter { it.track == track }
}
