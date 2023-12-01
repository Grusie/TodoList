package com.example.todolist.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.CustomFormatter
import com.example.todolist.R
import com.example.todolist.TodoListViewModel
import com.example.todolist.dateToString
import com.example.todolist.model.TodoData
import com.example.todolist.timeToString
import com.example.todolist.ui.theme.TodoListTheme
import com.example.todolist.uiState.EditTodoAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ExperimentalToolbarApi
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TodoListTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()/*, color = MaterialTheme.colorScheme.background*/
                ) {
                    MainScreen()
                }
            }
        }
    }

    /**
     * 메인페이지 스크린
     **/
    @OptIn(ExperimentalToolbarApi::class)
    @Composable
    fun MainScreen(viewModel: TodoListViewModel = viewModel()) {
        val todoUiState by viewModel.todoUiState.collectAsStateWithLifecycle()
        val todoListUiState by viewModel.todoListUiState.collectAsStateWithLifecycle()
        val state = rememberCollapsingToolbarScaffoldState()
        val scope = rememberCoroutineScope()
        var deleteFlag by rememberSaveable { mutableStateOf(false) }

        /*Scaffold(
            topBar = {
                AppTitleBar(
                    todoData = todoUiState.todoData,
                    deleteDataSet = todoUiState.deleteDataSet,
                    handleAction = { viewModel.dispatch(it) },
                    isShownToast = todoUiState.isShownToast
                )
            }
        ) { paddingValues ->
            CalendarView(handleAction = { viewModel.dispatch(it) })
            TodoList(
                modifier = Modifier.padding(paddingValues),
                todoList = todoUiState.todoList,
                selectedTodoItem = todoUiState.todoData,
                handleAction = { viewModel.dispatch(it) },
                isShownToast = todoUiState.isShownToast
            )
        }*/

        val monthFlag = state.toolbarState.progress > 0.1

        CollapsingToolbarScaffold(
            modifier = Modifier,
            state = state,
            toolbar = {
                Box(modifier = Modifier.height(70.dp)) {
                    CalendarHeader(
                        modifier = Modifier.fillMaxSize(),
                        currentDate = todoListUiState.currentDate,
                        changeCurrentMonth = {
                            viewModel.dispatch(it)
                            if (monthFlag) {
                                scope.launch {
                                    delay(200)
                                    state.toolbarState.expand(0)
                                }
                            }
                        },
                        formatPattern = if (monthFlag) CustomFormatter.monthFormat else CustomFormatter.dateFormat,
                        monthFlag = monthFlag
                    )
                }
                CalendarView(
                    modifier = Modifier
                        .pin()
                        .padding(top = 70.dp),
                    currentDate = todoListUiState.currentDate,
                    handleAction = {
                        viewModel.dispatch(it)
                        scope.launch {
                            delay(200)
                            state.toolbarState.expand(0)
                        }
                    }
                )
            },
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed
        ) {
            Column {
                AppTitleBar(
                    todoData = todoUiState.todoData,
                    deleteDataSet = todoListUiState.deleteDataSet,
                    handleAction = {
                        viewModel.dispatch(it)
                        if (it is EditTodoAction.DeleteTodoItem) deleteFlag = !deleteFlag
                    },
                    isShownToast = todoUiState.isShownToast,
                    currentDate = todoListUiState.currentDate
                )

                TodoList(
                    modifier = Modifier.fillMaxHeight(),
                    todoList = todoListUiState.todoList,
                    selectedTodoItem = todoUiState.todoData,
                    handleAction = {
                        viewModel.dispatch(it)
                        if (it is EditTodoAction.DeleteTodoItem) deleteFlag = !deleteFlag
                    },
                    isShownToast = todoUiState.isShownToast,
                    currentDate = todoListUiState.currentDate
                )
            }
        }
    }
}


/**
 * 앱 타이틀 바
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTitleBar(
    todoData: TodoData,
    deleteDataSet: MutableSet<Int>,
    handleAction: (EditTodoAction) -> Unit,
    isShownToast: Boolean,
    currentDate: LocalDate = LocalDate.now()
) {
    var showAddDialogState by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialogState by rememberSaveable { mutableStateOf(false) }

    TopAppBar(title = {
        Text(
            text = currentDate.dateToString(),
            fontSize = 20.sp
        )
    }, actions = {
        IconButton(onClick = {
            showAddDialogState = true
        }) {
            Icon(
                imageVector = Icons.Filled.Add, contentDescription = "addTodoList"
            )
        }
        IconButton(onClick = {
            if (deleteDataSet.isNotEmpty()) showDeleteDialogState = true
        }) {
            Icon(
                imageVector = Icons.Filled.Delete, contentDescription = "deleteTodoList"
            )
        }
    })

    if (showAddDialogState) {
        AddTodoDataDialog(
            handleAction = handleAction,
            selectedTodoData = todoData,
            closeDialog = { showAddDialogState = false },
            isShownToast = isShownToast,
            currentDate = currentDate
        )
    }

    if (showDeleteDialogState)
        DeleteDialog(
            id = -1,
            handleAction = handleAction,
            closeDialog = { showDeleteDialogState = false }
        )
}


/**
 * 투두데이터 삭제 다이얼로그
 **/
@Composable
fun DeleteDialog(
    id: Int = -1,
    handleAction: (EditTodoAction) -> Unit,
    closeDialog: () -> Unit
) {
    AlertDialog(onDismissRequest = { closeDialog() },
        confirmButton = {
            TextButton(onClick = {
                handleAction(EditTodoAction.DeleteTodoItem(id))
                closeDialog()
            }) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog() }) {
                Text(stringResource(id = R.string.cancel))
            }
        },
        title = { Text(text = stringResource(id = R.string.delete_todoData_title)) },
        text = { Text(text = stringResource(id = R.string.delete_todoData_detail)) })
}


/**
 * 투두 데이터 아이템
 **/
@Composable
fun TodoItem(
    todoData: TodoData,
    selectedTodoData: TodoData,
    handleAction: (EditTodoAction) -> Unit,
    isShownToast: Boolean,
    currentDate: LocalDate
) {
    var showAddTodoDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialogState by rememberSaveable { mutableStateOf(false) }

    if (showAddTodoDialog) {
        AddTodoDataDialog(
            selectedTodoData = selectedTodoData,
            handleAction = handleAction,
            closeDialog = { showAddTodoDialog = false },
            id = todoData.id,
            isShownToast = isShownToast,
            currentDate = currentDate
        )
    }

    if (showDeleteDialogState) {
        DeleteDialog(
            handleAction = handleAction,
            id = todoData.id,
            closeDialog = { showDeleteDialogState = false })
    }

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(enabled = true, onClick = {
                showAddTodoDialog = true
            })
    ) {

        var editable by rememberSaveable { mutableStateOf(false) }
        var isDone by rememberSaveable { mutableStateOf(false) }
        isDone = todoData.isDone
        Row(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                .padding(vertical = 8.dp)
        ) {
            Checkbox(
                checked = isDone, onCheckedChange = {
                    handleAction(EditTodoAction.UpdateTodoIsDone(todoData.id))
                }, modifier = Modifier.align(Alignment.CenterVertically)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = todoData.todoTime.timeToString(),
                    fontSize = 10.sp
                )
                Text(
                    text = todoData.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = todoData.description,
                    fontSize = 12.sp,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                IconButton(onClick = { editable = !editable }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(id = R.string.more)
                    )
                }
                DropdownMenu(
                    expanded = editable,
                    onDismissRequest = { editable = false },
                ) {
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.modify)) },
                        onClick = {
                            showAddTodoDialog = true
                            editable = false
                        })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.delete)) },
                        onClick = {
                            showDeleteDialogState = true
                            editable = false
                        })
                }
            }
        }
    }
}

/**
 * 투두데이터 추가 다이얼로그
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDataDialog(
    closeDialog: () -> Unit,
    handleAction: (EditTodoAction) -> Unit = {},
    selectedTodoData: TodoData = TodoData(),
    id: Int = -1,
    isShownToast: Boolean,
    currentDate: LocalDate
) {
    val updateFlag = id != -1
    val updatedTodoData = if (updateFlag) selectedTodoData else selectedTodoData
    //val focusRequest = remember { FocusRequester() }

    if (isShownToast) ShowToastMsg(message = stringResource(id = R.string.todoData_title))

    LaunchedEffect(Unit) {
        handleAction(EditTodoAction.SetTodoData(id))
        /*launch {
            focusRequest.requestFocus()
        }*/
    }

    AlertDialog(
        onDismissRequest = { closeDialog() },
        title = { Text(text = stringResource(id = R.string.add_todoData_title)) },
        confirmButton = {
            TextButton(onClick = {
                handleAction(EditTodoAction.ChangeTodoTitle(updatedTodoData.title))
                handleAction(EditTodoAction.ChangeTodoDescription(updatedTodoData.description))

                if (updatedTodoData.title.isEmpty()) {
                    handleAction(EditTodoAction.ShowToastMsg)
                } else {
                    handleAction(EditTodoAction.ModifyTodoData(updateFlag))
                    closeDialog()
                }
            }) {
                if (updateFlag) Text(text = stringResource(id = R.string.modify)) else Text(
                    text = stringResource(
                        id = R.string.add
                    )
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = updatedTodoData.title,
                    /*modifier = Modifier.focusRequester(focusRequest),*/
                    onValueChange = { handleAction(EditTodoAction.ChangeTodoTitle(it)) },
                    placeholder = { Text(stringResource(id = R.string.todoData_title)) },
                    maxLines = 5
                )
                OutlinedTextField(
                    value = updatedTodoData.description,
                    onValueChange = { handleAction(EditTodoAction.ChangeTodoDescription(it)) },
                    placeholder = { Text(stringResource(id = R.string.todoData_description)) },
                    modifier = Modifier
                        .height(300.dp)
                        .padding(vertical = 10.dp),
                    maxLines = 20
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { closeDialog() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        })
}

/**
 * 토스트 메세지
 **/
@Composable
fun ShowToastMsg(message: String) {
    Toast.makeText(LocalContext.current, message, Toast.LENGTH_SHORT).show()
}


/**
 * 투두 리스트
 **/
@Composable
fun TodoList(
    modifier: Modifier = Modifier,
    todoList: List<TodoData> = listOf(),
    handleAction: (EditTodoAction) -> Unit,
    isShownToast: Boolean,
    selectedTodoItem: TodoData,
    currentDate: LocalDate = LocalDate.now()
) {
    Surface(modifier = modifier) {
        LazyColumn() {
            if (todoList.isEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_empty_todo),
                            contentDescription = "empty todoList"
                        )
                        Text(text = stringResource(id = R.string.empty_todoList))
                    }
                }
            } else {
                items(items = todoList) { todoData ->
                    TodoItem(
                        todoData = todoData,
                        handleAction = handleAction,
                        selectedTodoData = selectedTodoItem,
                        isShownToast = isShownToast,
                        currentDate = currentDate
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 320)
@Composable
fun MainScreenPreview() {
    TodoListTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                topBar = {
                    CalendarView(currentDate = LocalDate.now(), handleAction = {})
                }
                /*topBar = {
                    CalendarHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                }*/
                /*topBar = {
                    AppTitleBar(
                        todoData = TodoData(),
                        deleteDataSet = mutableSetOf(),
                        handleAction = {},
                        isShownToast = false
                    )
                }*/
            ) { paddingValues ->
                TodoList(
                    todoList = listOf(TodoData(title = "title", description = "description")),
                    selectedTodoItem = TodoData(),
                    modifier = Modifier.padding(paddingValues),
                    isShownToast = false,
                    handleAction = {}
                )
            }
        }
    }
}
