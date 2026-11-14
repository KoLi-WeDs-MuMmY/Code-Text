package com.example.code

enum class DiffLineKind {
    UNCHANGED,
    ADDED,
    REMOVED
}

data class DiffLine(
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val kind: DiffLineKind,
    val text: String
)

data class DiffResult(
    val lines: List<DiffLine>,
    val additionsCount: Int,
    val deletionsCount: Int,
    val unchangedCount: Int
) {
    val hasChanges: Boolean get() = additionsCount > 0 || deletionsCount > 0
}

/**
 * Pure Kotlin LCS-based line diff engine for comparing LocalCodeSnapshots
 * against the active editor buffer in the Visual Git Diff & History Bar.
 */
object DiffEngine {

    fun computeLineDiff(oldText: String, newText: String): DiffResult {
        val oldLines = if (oldText.isEmpty()) emptyList() else oldText.replace("\r\n", "\n").split('\n')
        val newLines = if (newText.isEmpty()) emptyList() else newText.replace("\r\n", "\n").split('\n')

        val m = oldLines.size
        val n = newLines.size
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in m - 1 downTo 0) {
            for (j in n - 1 downTo 0) {
                dp[i][j] = if (oldLines[i] == newLines[j]) {
                    1 + dp[i + 1][j + 1]
                } else {
                    maxOf(dp[i + 1][j], dp[i][j + 1])
                }
            }
        }

        val out = mutableListOf<DiffLine>()
        var i = 0
        var j = 0
        var oldNum = 1
        var newNum = 1
        var adds = 0
        var dels = 0
        var same = 0

        while (i < m && j < n) {
            when {
                oldLines[i] == newLines[j] -> {
                    out.add(DiffLine(oldNum++, newNum++, DiffLineKind.UNCHANGED, oldLines[i]))
                    same++
                    i++
                    j++
                }
                dp[i + 1][j] >= dp[i][j + 1] -> {
                    out.add(DiffLine(oldNum++, null, DiffLineKind.REMOVED, oldLines[i]))
                    dels++
                    i++
                }
                else -> {
                    out.add(DiffLine(null, newNum++, DiffLineKind.ADDED, newLines[j]))
                    adds++
                    j++
                }
            }
        }
        while (i < m) {
            out.add(DiffLine(oldNum++, null, DiffLineKind.REMOVED, oldLines[i]))
            dels++
            i++
        }
        while (j < n) {
            out.add(DiffLine(null, newNum++, DiffLineKind.ADDED, newLines[j]))
            adds++
            j++
        }

        return DiffResult(
            lines = out,
            additionsCount = adds,
            deletionsCount = dels,
            unchangedCount = same
        )
    }
}
