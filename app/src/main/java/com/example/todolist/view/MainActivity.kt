package com.example.todolist.view

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.R
import com.example.todolist.TodoListViewModel
import com.example.todolist.model.TodoData
import com.example.todolist.ui.theme.TodoListTheme
import kotlinx.coroutines.delay

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


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(viewModel: TodoListViewModel = viewModel()) {
        val todoList by viewModel.todoList.collectAsState()
        Scaffold(
            //topBar = { AppTitleBar(viewModel = viewModel) }
        ) {
            Column {
                AppTitleBar(viewModel)
                TodoList(viewModel = viewModel, todoList = todoList)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppTitleBar(viewModel: TodoListViewModel = viewModel()) {
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
                if (viewModel.deleteItemsSet.size > 0)
                    showDeleteDialogState = true
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete, contentDescription = "deleteTodoList"
                )
            }
        })

        ShowTodoDialog(
            viewModel = viewModel,
            showDialog = showAddDialogState,
            onCloseDialog = { showAddDialogState = false }
        )

        if (showDeleteDialogState)
            DeleteDialog(
                viewModel = viewModel,
                id = -1,
                onCloseDialog = { showDeleteDialogState = false }
            )
    }


    @Composable
    fun DeleteDialog(viewModel: TodoListViewModel, id: Int = -1, onCloseDialog: () -> Unit) {
        AlertDialog(onDismissRequest = { onCloseDialog.invoke() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTodoItem(id)
                    onCloseDialog.invoke()
                }) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onCloseDialog.invoke() }) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            title = { Text(text = stringResource(id = R.string.delete_todoData_title)) },
            text = { Text(text = stringResource(id = R.string.delete_todoData_detail)) })
    }

    @Composable
    fun TodoItem(viewModel: TodoListViewModel, item: TodoData) {
        var showDialog by rememberSaveable { mutableStateOf(false) }

        ShowTodoDialog(
            viewModel = viewModel,
            showDialog = showDialog,
            onCloseDialog = { showDialog = false },
            id = item.id
        )
        Card(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .clickable(enabled = true, onClick = {
                    showDialog = true
                })
        ) { TodoContent(viewModel, item) }
    }

    @Composable
    fun ShowTodoDialog(
        viewModel: TodoListViewModel, showDialog: Boolean, onCloseDialog: () -> Unit, id: Int = -1
    ) {
        if (showDialog) {
            viewModel.setTodoData(id)
            AddTodoDataDialog(
                viewModel = viewModel, closeDialog = onCloseDialog, id = id
            )
        }
    }


    @Composable
    fun TodoContent(viewModel: TodoListViewModel, item: TodoData) {
        var editable by rememberSaveable { mutableStateOf(false) }
        var isDone by rememberSaveable { mutableStateOf(false) }
        isDone = item.isDone
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
                    viewModel.updateTodoIsDone(item.id)
                }, modifier = Modifier.align(Alignment.CenterVertically)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                var showAddDialogState by rememberSaveable { mutableStateOf(false) }
                var showDeleteDialogState by rememberSaveable { mutableStateOf(false) }
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
                            showAddDialogState = true
                            editable = false
                        })
                    DropdownMenuItem(text = { Text(stringResource(id = R.string.delete)) },
                        onClick = {
                            showDeleteDialogState = true
                            editable = false
                        })
                }

                ShowTodoDialog(
                    viewModel = viewModel,
                    showDialog = showAddDialogState,
                    onCloseDialog = { showAddDialogState = false },
                    id = item.id
                )

                if (showDeleteDialogState) DeleteDialog(
                    viewModel = viewModel,
                    id = item.id,
                    onCloseDialog = { showDeleteDialogState = false })
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AddTodoDataDialog(
        viewModel: TodoListViewModel, closeDialog: () -> Unit, id: Int = -1
    ) {
        val updateFlag = id != -1
        val activity = (LocalContext.current as Activity)
        val todoData = viewModel.todoData.collectAsState().value
        var emptyTitleState by rememberSaveable { mutableStateOf(false) }
        var isToastVisibleState by rememberSaveable { mutableStateOf(false) }
        val focusRequest = remember {FocusRequester()}

        if (emptyTitleState && !isToastVisibleState) {
            isToastVisibleState = true
            Toast.makeText(
                activity,
                stringResource(id = R.string.todoData_title),
                Toast.LENGTH_SHORT
            ).show()
        }

        LaunchedEffect(emptyTitleState) {
            delay(2000)
            isToastVisibleState = false
        }
        AlertDialog(
            onDismissRequest = { /*closeDialog.invoke()*/ },
            title = { Text(text = stringResource(id = R.string.add_todoData_title)) },
            confirmButton = {
                TextButton(onClick = {
                    if (viewModel.todoData.value.title.isEmpty()) {
                        emptyTitleState = true
                    } else {
                        viewModel.modifyTodoData(updateFlag, todoData)
                        closeDialog.invoke()
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
                        value = todoData.title,
                        modifier = Modifier.focusRequester(focusRequest),
                        onValueChange = { viewModel.updateTodoTitle(it) },
                        placeholder = { Text(stringResource(id = R.string.todoData_title)) },
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = todoData.description,
                        onValueChange = { viewModel.updateTodoDescription(it) },
                        placeholder = { Text(stringResource(id = R.string.todoData_description)) },
                        modifier = Modifier
                            .height(300.dp)
                            .padding(vertical = 10.dp),
                        maxLines = 20
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { closeDialog.invoke() }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            })
        focusRequest.requestFocus()
    }

    @Composable
    fun TodoList(viewModel: TodoListViewModel = viewModel(), todoList: List<TodoData> = listOf()) {
        LazyColumn() {
            items(items = todoList) { todoData ->
                TodoItem(viewModel = viewModel, item = todoData)
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Preview(showBackground = true, widthDp = 320, uiMode = UI_MODE_NIGHT_YES)
    @Composable
    fun MainScreenPreview() {
        TodoListTheme {
            Scaffold(
                topBar = { AppTitleBar() },
            ) {
                Column {
                    AppTitleBar()
                    //TodoList()
                }
            }
        }
    }
}