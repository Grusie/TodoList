package com.example.todolist.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.C5586EB
import com.example.todolist.ui.theme.CF15F5F
import com.example.todolist.ui.theme.TodoListTheme
import com.example.todolist.uiState.EditTodoAction
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun Calendar(
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now(),
    handleAction: (EditTodoAction) -> Unit
) {

    val firstDayOfCurrentMonth = LocalDate.of(currentDate.year, currentDate.month, 1)
    val firstDayOfWeek =
        if (firstDayOfCurrentMonth.dayOfWeek.value == 7) 0 else firstDayOfCurrentMonth.dayOfWeek.value - 1
    val yearMonth = YearMonth.of(currentDate.year, currentDate.month)

    var selectedDate by remember { mutableStateOf(currentDate) }

    LaunchedEffect(currentDate) {
        selectedDate = currentDate
    }

    Column(modifier = modifier) {
        Column(Modifier.padding(8.dp)) {
            val reorderedDays = DayOfWeek.values().toMutableList()
            reorderedDays.remove(DayOfWeek.SUNDAY)
            reorderedDays.add(0, DayOfWeek.SUNDAY)

            // 요일을 표시하는 Row
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                reorderedDays.forEach { dayOfWeek ->
                    val textName = dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.NARROW,
                        Locale.KOREAN
                    )
                    Text(
                        text = textName,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        color = if (dayOfWeek == DayOfWeek.SUNDAY) CF15F5F else if (dayOfWeek == DayOfWeek.SATURDAY) C5586EB else LocalContentColor.current
                    )
                }
            }

            // 날짜를 표시하는 Row들
            var dayOfMonth = 1
            val maxLines = (firstDayOfWeek + currentDate.lengthOfMonth()) / 7
            var line = 0
            while (line <= maxLines) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    Arrangement.SpaceAround,
                ) {
                    for (i in 0 until 7) {
                        // 요일 시작 전 빈 박스를 표시하는 Row

                        if (line == 0 && i <= firstDayOfWeek) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .size(45.dp)
                            )
                        } else if (dayOfMonth > currentDate.lengthOfMonth()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .size(45.dp)
                            )
                        } else {
                            val date = yearMonth.atDay(dayOfMonth)
                            val isSelected = date == selectedDate


                            CalendarDay(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .size(45.dp),
                                date = date,
                                isSelected = isSelected,
                                onDateClick = {
                                    handleAction(EditTodoAction.UpdateCurrentDate(date))
                                    handleAction(EditTodoAction.UpdateTodoList(date))
                                }
                            )
                            dayOfMonth++
                        }
                    }
                    line++
                }
            }
        }


        /*LazyVerticalGrid(
            modifier = Modifier.padding(8.dp),
            columns = GridCells.Fixed(7)
        ) {
            val reorderedDays = DayOfWeek.values().toMutableList()
            reorderedDays.remove(DayOfWeek.SUNDAY)
            reorderedDays.add(0, DayOfWeek.SUNDAY)

            reorderedDays.forEach { dayOfWeek ->
                //NARROW : 요일 표시 방법을 Sunday가 아니라 S처럼 짧게 표시
                //Locale.KOREAN : 월,화,수,목,금,토,일 처럼 한글로 설정
                item {
                    Text(
                        text = dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.NARROW,
                            Locale.KOREAN
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (firstDayOfWeek < 7) {
                for (i in 0 until firstDayOfWeek) { // 처음 날짜가 시작하는 요일 전까지 빈 박스 생성
                    item {
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    }
                }
            }

            items(currentDate.lengthOfMonth()) { dayOfMonth ->
                val date = yearMonth.atDay(dayOfMonth + 1)
                val isSelected = date == selectedDate

                CalendarDay(
                    date = date,
                    isSelected = isSelected,
                    onDateClick = {
                        selectedDate = date
                        handleAction(EditTodoAction.UpdateTodoList(selectedDate.dateToString()))
                    }
                )
            }
        }*/
    }
}

@Composable
fun CalendarDay(
    modifier: Modifier,
    date: LocalDate,
    isSelected: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
        label = "calendar item color animation"
    )
    Box(
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .background(backgroundColor)
            .clickable { onDateClick(date) }
    ) {
        val defaultColor =
            if (date.dayOfWeek == DayOfWeek.SUNDAY) CF15F5F else if (date.dayOfWeek == DayOfWeek.SATURDAY) C5586EB else Color.Black

        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.Center),
            color = if (isSelected) Color.White else defaultColor
        )
    }
}

@Composable
fun CalendarSimple(modifier: Modifier = Modifier, currentDate: String) {
    Row(
        modifier = modifier.background(MaterialTheme.colorScheme.secondaryContainer),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = currentDate, fontSize = 20.sp, color = Color.White)
    }
}

@Composable
fun CalendarHeader(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    changeCurrentMonth: (EditTodoAction) -> Unit,
    formatPattern: DateTimeFormatter,
    monthFlag: Boolean
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {
                val resultDate =
                    if (monthFlag) currentDate.minusMonths(1) else currentDate.minusDays(1)
                changeCurrentMonth(
                    EditTodoAction.UpdateCurrentDate(
                        resultDate
                    )
                )
                changeCurrentMonth(
                    EditTodoAction.UpdateTodoList(
                        resultDate
                    )
                )
            },
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "lastDay")
        }
        Text(
            text = currentDate.format(formatPattern),
            modifier = Modifier,
            style = TextStyle(fontWeight = FontWeight.Bold),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = {
                val resultDate =
                    if (monthFlag) currentDate.plusMonths(1) else currentDate.plusDays(1)
                changeCurrentMonth(
                    EditTodoAction.UpdateCurrentDate(
                        resultDate
                    )
                )
                changeCurrentMonth(
                    EditTodoAction.UpdateTodoList(resultDate)
                )
            },
            modifier = Modifier
                .size(24.dp)
        ) {
            Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "pastDay")
        }
    }
}

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    handleAction: (EditTodoAction) -> Unit,
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Calendar(
                currentDate = currentDate,
                handleAction = handleAction
            )
        }
    }
}

@Preview
@Composable
fun CalendarPreview() {
    TodoListTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CalendarView(currentDate = LocalDate.now(), handleAction = {})
        }
    }
}

@Preview
@Composable
fun CalendarSimplePreview() {
    TodoListTheme {
        CalendarSimple(currentDate = "2023년 11월 10일")
    }
}