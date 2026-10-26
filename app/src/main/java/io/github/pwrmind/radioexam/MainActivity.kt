package io.github.pwrmind.radioexam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.pwrmind.radioexam.ui.theme.RadioExamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _root_ide_package_.io.github.pwrmind.radioexam.ui.theme.RadioExamTheme {
                _root_ide_package_.io.github.pwrmind.radioexam.RadioExamApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioExamApp() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showTopLevelChrome = currentRoute == _root_ide_package_.io.github.pwrmind.radioexam.Screen.Catalog.route ||
            currentRoute == _root_ide_package_.io.github.pwrmind.radioexam.Screen.MyTests.route

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (showTopLevelChrome) {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(Screen.Catalog.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                if (showTopLevelChrome) {
                    LargeTopAppBar(
                        title = {
                            Text(
                                text = when (currentRoute) {
                                    Screen.Catalog.route -> "Витрина тестов"
                                    Screen.MyTests.route -> "Моя подготовка"
                                    else -> "Радио Экзамен"
                                }
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Catalog.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable(Screen.Catalog.route) {
                    CatalogScreen(
                        onTestClick = { sectionId ->
                            navController.navigate(Screen.Preview.createRoute(sectionId))
                        }
                    )
                }

                composable(Screen.MyTests.route) {
                    MyTestsScreen(
                        onContinueTestClick = { sectionId ->
                            navController.navigate(Screen.Quiz.createRoute(sectionId))
                        }
                    )
                }

                composable(
                    route = Screen.Preview.route,
                    arguments = listOf(navArgument("sectionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
                    PreviewScreen(
                        sectionId = sectionId,
                        onBackClick = { navController.popBackStack() },
                        onStartTestClick = { id ->
                            navController.navigate(Screen.Quiz.createRoute(id))
                        },
                        onTheoryClick = { id ->
                            navController.navigate(Screen.Theory.createRoute(id))
                        }
                    )
                }

                composable(
                    route = Screen.Theory.route,
                    arguments = listOf(navArgument("sectionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
                    TheoryScreen(
                        sectionId = sectionId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Quiz.route,
                    arguments = listOf(navArgument("sectionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
                    QuizScreen(
                        sectionId = sectionId,
                        onCloseQuiz = { navController.popBackStack() },
                        onFinishQuiz = { score, total ->
                            navController.navigate(Screen.Result.createRoute(score, total)) {
                                popUpTo(Screen.Quiz.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = Screen.Result.route,
                    arguments = listOf(
                        navArgument("score") { type = NavType.IntType },
                        navArgument("total") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val score = backStackEntry.arguments?.getInt("score") ?: 0
                    val total = backStackEntry.arguments?.getInt("total") ?: 0

                    ResultScreen(
                        correctAnswers = score,
                        totalQuestions = total,
                        onRetryClick = {
                            val popped = navController.popBackStack(
                                route = Screen.Preview.route,
                                inclusive = false
                            )
                            if (!popped) {
                                navController.navigate(Screen.Catalog.route) {
                                    popUpTo(Screen.Catalog.route) { inclusive = true }
                                }
                            }
                        },
                        onGoHomeClick = {
                            navController.navigate(Screen.Catalog.route) {
                                popUpTo(Screen.Catalog.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}