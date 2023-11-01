package com.example.todolist.view

import android.annotation.SuppressLint
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.R
import com.example.todolist.TodoListViewModel
import com.example.todolist.model.TodoData
import com.example.todolist.ui.theme.TodoListTheme

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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(viewModel: TodoListViewModel = viewModel()) {
        val todoUiState by viewModel.todoUiState.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                AppTitleBar(
                    todoData = todoUiState.todoData,
                    deleteDataSet = todoUiState.deleteDataSet,
                    setTodoData = { id -> viewModel.setTodoData(id) },
                    onTitleChanged = { title -> viewModel.updateTodoTitle(title) },
                    onDescriptionChanged = { description ->
                        viewModel.updateTodoDescription(
                            description
                        )
                    },
                    isShownToast = todoUiState.isShownToast,
                    modifyTodoData = { updateFlag, todoData ->
                        viewModel.modifyTodoData(
                            updateFlag,
                            todoData
                        )
                    },
                    deleteTodoItem = { id -> viewModel.deleteTodoData(id) })
            }
        ) { paddingValues ->
            TodoList(
                modifier = Modifier.padding(paddingValues),
                todoList = todoUiState.todoList,
                selectedTodoItem = todoUiState.todoData,
                onTitleChanged = { title -> viewModel.updateTodoTitle(title) },
                onDescriptionChanged = { description ->
                    viewModel.updateTodoDescription(
                        description
                    )
                },
                isShownToast = todoUiState.isShownToast,
                modifyTodoData = { updateFlag, todoData ->
                    viewModel.modifyTodoData(
                        updateFlag,
                        todoData
                    )
                },
                setTodoData = { id -> viewModel.setTodoData(id) },
                updateTodoIsDone = { id -> viewModel.updateTodoIsDone(id) },
                deleteTodoItem = { id -> viewModel.deleteTodoData(id) })
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
        setTodoData: (Int) -> Unit,
        onTitleChanged: (String) -> Unit,
        onDescriptionChanged: (String) -> Unit,
        isShownToast: Boolean,
        modifyTodoData: (Boolean, TodoData) -> Boolean,
        deleteTodoItem: (Int) -> Unit
    ) {
        var showAddDialogState by rememberSaveable { mutableStateOf(false) }
        var showDeleteDialogState by rememberSaveable { mutableStateOf(false) }

        TopAppBar(title = {
            Text(
                text = stringResource(id = R.string.app_name)
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
                todoData = todoData,
                closeDialog = { showAddDialogState = false },
                onTitleChanged = onTitleChanged,
                onDescriptionChanged = onDescriptionChanged,
                setTodoData = setTodoData,
                isShownToast = isShownToast,
                modifyTodoData = modifyTodoData
            )
        }

        if (showDeleteDialogState)
            DeleteDialog(
                id = -1,
                closeDialog = { showDeleteDialogState = false },
                deleteTodoItem = deleteTodoItem
            )
    }


    /**
     * 투두데이터 삭제 다이얼로그
     **/
    @Composable
    fun DeleteDialog(
        id: Int = -1,
        deleteTodoItem: (Int) -> Unit,
        closeDialog: () -> Unit
    ) {
        AlertDialog(onDismissRequest = { closeDialog() },
            confirmButton = {
                TextButton(onClick = {
                    deleteTodoItem(id)
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
        onTitleChanged: (String) -> Unit,
        onDescriptionChanged: (String) -> Unit,
        isShownToast: Boolean,
        modifyTodoData: (Boolean, TodoData) -> Boolean,
        setTodoData: (Int) -> Unit,
        updateTodoIsDone: (Int) -> Unit,
        deleteTodoItem: (Int) -> Unit
    ) {
        var showAddTodoDialog by rememberSaveable { mutableStateOf(false) }
        var showDeleteDialogState by rememberSaveable { mutableStateOf(false) }

        if (showAddTodoDialog) {
            AddTodoDataDialog(
                todoData = todoData,
                selectedTodoData = selectedTodoData,
                setTodoData = { setTodoData(it) },
                closeDialog = { showAddTodoDialog = false },
                id = todoData.id,
                onTitleChanged = onTitleChanged,
                onDescriptionChanged = onDescriptionChanged,
                isShownToast = isShownToast,
                modifyTodoData = modifyTodoData
            )
        }

        if (showDeleteDialogState) {
            DeleteDialog(
                deleteTodoItem = deleteTodoItem,
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
                        updateTodoIsDone(todoData.id)
                    }, modifier = Modifier.align(Alignment.CenterVertically)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
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
        todoData: TodoData,
        closeDialog: () -> Unit,
        selectedTodoData: TodoData = TodoData(),
        id: Int = -1,
        onTitleChanged: (String) -> Unit,
        onDescriptionChanged: (String) -> Unit,
        setTodoData: (Int) -> Unit,
        isShownToast: Boolean,
        modifyTodoData: (Boolean, TodoData) -> Boolean
    ) {
        val updateFlag = id != -1
        val updatedTodoData = if (updateFlag) selectedTodoData else todoData
        //val focusRequest = remember { FocusRequester() }

        if (isShownToast) ShowToastMsg(message = stringResource(id = R.string.todoData_title))

        LaunchedEffect(Unit) {
            setTodoData(id)
            /*launch {
                focusRequest.requestFocus()
            }*/
        }

        AlertDialog(
            onDismissRequest = { closeDialog() },
            title = { Text(text = stringResource(id = R.string.add_todoData_title)) },
            confirmButton = {
                TextButton(onClick = {
                    onTitleChanged(updatedTodoData.title)
                    onDescriptionChanged(updatedTodoData.description)
                    if (modifyTodoData(updateFlag, updatedTodoData)) closeDialog.invoke()
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
                        onValueChange = { onTitleChanged(it) },
                        placeholder = { Text(stringResource(id = R.string.todoData_title)) },
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = updatedTodoData.description,
                        onValueChange = { onDescriptionChanged(it) },
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
        onTitleChanged: (String) -> Unit,
        onDescriptionChanged: (String) -> Unit,
        isShownToast: Boolean,
        selectedTodoItem: TodoData,
        modifyTodoData: (Boolean, TodoData) -> Boolean,
        setTodoData: (Int) -> Unit,
        updateTodoIsDone: (Int) -> Unit,
        deleteTodoItem: (Int) -> Unit
    ) {
        LazyColumn(modifier = modifier) {
            items(items = todoList) { todoData ->
                TodoItem(
                    todoData = todoData,
                    onTitleChanged = onTitleChanged,
                    onDescriptionChanged = onDescriptionChanged,
                    selectedTodoData = selectedTodoItem,
                    isShownToast = isShownToast,
                    modifyTodoData = modifyTodoData,
                    setTodoData = setTodoData,
                    updateTodoIsDone = updateTodoIsDone,
                    deleteTodoItem = deleteTodoItem
                )
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Preview(showBackground = true, widthDp = 320, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun MainScreenPreview() {
        TodoListTheme {
            Surface(
                modifier = Modifier.fillMaxSize()/*, color = MaterialTheme.colorScheme.background*/
            ) {
                TodoList(
                    todoList = listOf(TodoData(title = "title", description = "description")),
                    selectedTodoItem = TodoData(),
                    setTodoData = {},
                    modifier = Modifier,
                    deleteTodoItem = {},
                    modifyTodoData = {_,_ -> false},
                    isShownToast = false,
                    onDescriptionChanged = {},
                    onTitleChanged = {},
                    updateTodoIsDone = {}
                )
            }
        }
    }
}