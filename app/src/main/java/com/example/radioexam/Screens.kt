package com.example.radioexam

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// --- 1. КАТАЛОГ ТЕСТОВ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(onTestClick: (String) -> Unit, viewModel: CatalogViewModel = viewModel()) {
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSections()
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (sections.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "База данных пуста.\nПопробуйте удалить приложение с эмулятора и установить заново.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            items(sections) { section ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onTestClick(section.id) },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = section.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "Вопросов: ${section.pickCount}",
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (section.activeAttemptId != null &&
                                section.totalInAttempt > 0
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val progress = section.answeredCount.toFloat() /
                                        section.totalInAttempt.toFloat()
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Отвечено ${section.answeredCount} из ${section.totalInAttempt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- 2. МОИ ТЕСТЫ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTestsScreen(
    onContinueTestClick: (String) -> Unit,
    viewModel: MyTestsViewModel = viewModel()
) {
    val inProgress by viewModel.inProgressTests.collectAsState()
    val completed by viewModel.completedTests.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTests()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        if (inProgress.isNotEmpty()) {
            item {
                Text(
                    text = "Продолжить подготовку",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            items(inProgress) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.sectionName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (item.totalCount > 0) {
                                    "Отвечено ${item.answeredCount} из ${item.totalCount}"
                                } else {
                                    "В процессе"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { onContinueTestClick(item.sectionId) },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Войти", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        if (completed.isNotEmpty()) {
            item {
                Text(
                    text = "Пройденные тесты",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
            items(completed) { item ->
                val hasScore = item.totalCount > 0
                val percent = if (hasScore) {
                    (item.correctCount.toFloat() / item.totalCount * 100).toInt()
                } else 0
                val isPassed = percent >= 70

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (hasScore) {
                                if (isPassed) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.sectionName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hasScore) {
                                Text(
                                    text = "${item.correctCount} из ${item.totalCount} • $percent%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isPassed) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isPassed) "Сдан" else "Не сдан",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    text = "Завершён",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (inProgress.isEmpty() && completed.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "У вас пока нет пройденных\nили начатых тестов",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- 3. ПРЕВЬЮ ТЕСТА ---
@Composable
fun PreviewScreen(
    sectionId: String,
    onBackClick: () -> Unit,
    onStartTestClick: (String) -> Unit,
    onTheoryClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Подготовка к тесту",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Вы собираетесь запустить случайную выборку вопросов по направлению:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = sectionId,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onStartTestClick(sectionId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Начать прохождение", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onTheoryClick(sectionId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Изучить теорию", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Назад к витрине")
                }
            }
        }
    }
}

// --- 4. ЭКРАН ТЕСТИРОВАНИЯ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    sectionId: String,
    onCloseQuiz: () -> Unit,
    onFinishQuiz: (Int, Int) -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val answers by viewModel.currentAnswers.collectAsState()
    val score by viewModel.score.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val selectedAnswerId by viewModel.currentSelectedAnswerId.collectAsState()
    val formattedTime by viewModel.formattedTime.collectAsState()
    val timeExpired by viewModel.timeExpired.collectAsState()

    val haptic = LocalHapticFeedback.current
    var showHintSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sectionId) {
        viewModel.loadTest(sectionId)
    }

    LaunchedEffect(timeExpired) {
        if (timeExpired && questions.isNotEmpty()) {
            onFinishQuiz(score, questions.size)
        }
    }

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentQuestion = questions[currentIndex]
    val currentState = userAnswers[currentIndex]
    val isAnswered = currentState?.status == QuestionStatus.CORRECT ||
            currentState?.status == QuestionStatus.INCORRECT

    if (showHintSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHintSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Подсказка",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentQuestion.hint ?: "Для этого вопроса нет подсказки.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(questions) { index, _ ->
                val state = userAnswers[index]
                val isCurrent = index == currentIndex

                val containerColor = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    state?.status == QuestionStatus.CORRECT -> MaterialTheme.colorScheme.tertiary
                    state?.status == QuestionStatus.INCORRECT -> MaterialTheme.colorScheme.error
                    state?.status == QuestionStatus.SKIPPED -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                }
                val textColor = when {
                    isCurrent -> MaterialTheme.colorScheme.onPrimary
                    state?.status == QuestionStatus.CORRECT -> MaterialTheme.colorScheme.onTertiary
                    state?.status == QuestionStatus.INCORRECT -> MaterialTheme.colorScheme.onError
                    state?.status == QuestionStatus.SKIPPED -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(containerColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.jumpToQuestion(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = textColor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLowTime = formattedTime.startsWith("00:")
            Surface(
                color = if (isLowTime) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isLowTime) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowTime) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentQuestion.text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { showHintSheet = true },
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Показать подсказку", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            answers.forEach { answer ->
                val isSelected = selectedAnswerId == answer.id

                val containerColor = when {
                    isAnswered && answer.isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                    isAnswered && isSelected && !answer.isCorrect -> MaterialTheme.colorScheme.errorContainer
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }
                val contentColor = when {
                    isAnswered && answer.isCorrect -> MaterialTheme.colorScheme.onTertiaryContainer
                    isAnswered && isSelected && !answer.isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                val border = when {
                    isAnswered && answer.isCorrect -> BorderStroke(
                        2.dp, MaterialTheme.colorScheme.tertiary
                    )
                    isAnswered && isSelected && !answer.isCorrect -> BorderStroke(
                        2.dp, MaterialTheme.colorScheme.error
                    )
                    isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else -> null
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = containerColor,
                    border = border,
                    onClick = {
                        if (!isAnswered) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectAnswer(answer.id)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = answer.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAnswered) {
                            Spacer(modifier = Modifier.width(12.dp))
                            when {
                                answer.isCorrect -> Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Правильно",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                isSelected -> Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Неправильно",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isAnswered) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.skipQuestion()
                    },
                    modifier = Modifier.height(52.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Пропустить")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!isAnswered) {
                        viewModel.checkAnswer()
                    } else {
                        if (currentIndex < questions.size - 1) {
                            viewModel.jumpToQuestion(currentIndex + 1)
                        } else {
                            viewModel.finishTest()
                            onFinishQuiz(score, questions.size)
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                enabled = selectedAnswerId != null || isAnswered,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = when {
                        !isAnswered -> "Проверить"
                        currentIndex < questions.size - 1 -> "Далее"
                        else -> "Результаты"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        TextButton(
            onClick = onCloseQuiz,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Прервать тестирование",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// --- 5. ТЕОРИЯ ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryScreen(
    sectionId: String,
    onBackClick: () -> Unit,
    viewModel: TheoryViewModel = viewModel()
) {
    LaunchedEffect(sectionId) {
        viewModel.load(sectionId)
    }

    val sectionName by viewModel.sectionName.collectAsState()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Теория", style = MaterialTheme.typography.titleLarge)
                    if (sectionName.isNotEmpty()) {
                        Text(
                            text = sectionName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "В этой секции пока нет теоретического материала.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                items(items.size) { index ->
                    TheoryCard(item = items[index], number = index + 1)
                }
            }
        }
    }
}

@Composable
private fun TheoryCard(item: TheoryItem, number: Int) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "№ $number",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.question.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.correctAnswer?.text ?: "Правильный ответ не задан",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    if (!item.question.hint.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.question.hint!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (!item.question.hint.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (expanded) "Скрыть подсказку" else "Показать подсказку",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

// --- 6. РЕЗУЛЬТАТЫ ---
@Composable
fun ResultScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    onRetryClick: () -> Unit,
    onGoHomeClick: () -> Unit
) {
    val targetPercentage =
        if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100).toInt() else 0
    val animatedPercentage by animateFloatAsState(
        targetValue = targetPercentage.toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "resultProgress"
    )

    val isPassed = targetPercentage >= 70
    val scoreColor = if (isPassed) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.error
    val statusText = if (isPassed) "Экзамен успешно сдан!" else "Тест не пройден"

    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = scoreColor
        )
        Spacer(modifier = Modifier.height(40.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animatedPercentage / 100f },
                modifier = Modifier.size(180.dp),
                color = scoreColor,
                strokeWidth = 14.dp,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${animatedPercentage.toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$correctAnswers из $totalQuestions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Повторить тест", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGoHomeClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Вернуться на главную", style = MaterialTheme.typography.titleMedium)
        }
    }
}