package io.github.pwrmind.radioexam

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Catalog : Screen("catalog")
    object MyTests : Screen("my_tests")
    object Preview : Screen("preview/{sectionId}") {
        fun createRoute(sectionId: String) = "preview/$sectionId"
    }
    object Quiz : Screen("quiz/{sectionId}") {
        fun createRoute(sectionId: String) = "quiz/$sectionId"
    }

    // Результаты принимают два числа
    object Result : Screen("result/{score}/{total}") {
        fun createRoute(score: Int, total: Int) = "result/$score/$total"
    }

    object Theory : Screen("theory/{sectionId}") {
        fun createRoute(sectionId: String) = "theory/$sectionId"
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    CATALOG("Витрина", Icons.Default.Search, Screen.Catalog.route),
    MY_TESTS("Мои тесты", Icons.AutoMirrored.Filled.List, Screen.MyTests.route)
}