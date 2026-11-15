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
            instructions = "Write a Kotlin `main()` function that sums all numbers from `1..5` into a variable named `total`, then prints `Total=15`.",
            hint = "Declare `var total = 0`, iterate `for (i in 1..5) { total += i }`, and call `println(\"Total=\$total\")`.",
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
            instructions = "Build a Jetpack Compose `Card` containing a `Column`, a greeting `Text(\"Student Profile\")`, and an interactive `Button(\"Enroll\")`. Open the `UI Preview` tab to see it render live!",
            hint = "Use `Column { Card { Column { Text(\"Student Profile\") Button(onClick = {}) { Text(\"Enroll\") } } } }`.",
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
            id = "dart_flutter_widget",
            track = LearningTrack.FLUTTER_DART,
            title = "Flutter Widget Tree & Text",
            conceptTag = "Flutter Widget Tree",
            difficulty = "Beginner",
            language = LanguageType.DART,
            instructions = "Create a Flutter-style widget tree with `Scaffold`, `AppBar(title: Text('Study App'))`, and a `Column` showing `Text('Module 1')` plus an `ElevatedButton` labeled `'Start'`.",
            hint = "Open the `UI Preview` drawer tab as you type to see your Flutter `Scaffold` and `Column` update instantaneously.",
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
