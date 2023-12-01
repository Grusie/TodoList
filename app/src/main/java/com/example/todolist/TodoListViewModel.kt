package com.example.todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.model.TodoDao
import com.example.todolist.model.TodoData
import com.example.todolist.model.TodoDatabase
import com.example.todolist.uiState.EditTodoAction
import com.example.todolist.uiState.TodoListUiState
import com.example.todolist.uiState.TodoUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class TodoListViewModel(application: Application) : AndroidViewModel(application) {
    private val _todoUiState = MutableStateFlow(TodoUiState())
    private val _todoListUiState = MutableStateFlow(TodoListUiState())
    private val todoDb: TodoDatabase = TodoDatabase.getInstance(application)
    private val todoDao: TodoDao = todoDb.todoDao()

    val todoListUiState: StateFlow<TodoListUiState> = _todoListUiState
    val todoUiState: StateFlow<TodoUiState> = _todoUiState

    private var currentJob: Job? = null

    init {
        updateTodoList(_todoListUiState.value.currentDate)
    }

    fun dispatch(action: EditTodoAction) {
        when (action) {
            is EditTodoAction.ChangeTodoTitle -> {
                updateTodoTitle(action.title)
            }

            is EditTodoAction.ChangeTodoDescription -> {
                updateTodoDescription(action.description)
            }

            is EditTodoAction.UpdateTodoIsDone -> {
                updateTodoIsDone(action.id)
            }

            is EditTodoAction.ShowToastMsg -> {
                showToast()
            }

            is EditTodoAction.SetTodoData -> {
                setTodoData(action.id)
            }

            is EditTodoAction.ModifyTodoData -> {
                modifyTodoData(action.updateFlag)
            }

            is EditTodoAction.DeleteTodoItem -> {
                deleteTodoData(action.id)
            }

            is EditTodoAction.UpdateTodoList -> {
                updateTodoList(action.selectedDate)
            }

            is EditTodoAction.UpdateCurrentDate -> {
                updateCurrentDate(action.date)
            }
        }
    }

    /**
     * 투두데이터 세팅
     **/
    private fun setTodoData(id: Int) {
        if (id == -1) {
            _todoUiState.update { currentTodoUiState ->
                currentTodoUiState.copy(
                    todoData = TodoData(todoDate = _todoListUiState.value.currentDate)
                )
            }
        } else {
            viewModelScope.launch {
                _todoUiState.update { currentTodoUiState ->
                    currentTodoUiState.copy(
                        todoData = getTodoData(id)
                    )
                }
            }
        }
    }

    /**
     * 투두데이터 삽입/수정
     **/
    private fun modifyTodoData(updateFlag: Boolean = false) {
        /*        _todoUiState.update { currentTodoUiState ->
                    currentTodoUiState.copy(
                        todoData = currentTodoUiState.todoData.copy(writtenTime = LocalDateTime.now())
                    )
                }*/
        val updatedTodoData = _todoUiState.value.todoData.copy(writtenTime = LocalDateTime.now())
        viewModelScope.launch {
            if (updateFlag) updateTodoData() else insertTodoData(updatedTodoData)
        }
    }

    /**
     * 투두 데이터 삽입
     **/
    private suspend fun insertTodoData(todoData: TodoData) {
        todoDao.insert(todoData)
    }

    /**
     * 투두 데이터 타이틀 수정
     **/
    private fun updateTodoTitle(title: String) {
        _todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                todoData = currentTodoUiState.todoData.copy(title = title)
            )
        }

        /*        _todoUiState2.update {
                    TodoUiState2.Success(title)
                }*/
    }

    /**
     * 투두 데이터 내용 수정
     **/
    private fun updateTodoDescription(description: String) {
        _todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                todoData = currentTodoUiState.todoData.copy(description = description)
            )
        }
    }

    /**
     * 투두 데이터 체크 상태 업데이트
     **/
    private fun updateTodoIsDone(id: Int) {
        viewModelScope.launch {
            _todoUiState.update { currentTodoUiState ->
                val todoData = getTodoData(id)
                val updatedTodoData = todoData.copy(isDone = !todoData.isDone)
                if (updatedTodoData.isDone) _todoListUiState.value.deleteDataSet.add(id) else _todoListUiState.value.deleteDataSet.remove(
                    id
                )

                currentTodoUiState.copy(
                    todoData = updatedTodoData
                )
            }

            updateTodoData()
        }
    }

    /**
     * 토스트 메세지 관리
     **/
    private fun showToast() {
        _todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                isShownToast = true
            )
        }
        clearShowToast()
    }

    private fun clearShowToast() {
        viewModelScope.launch {
            delay(2000)
            _todoUiState.update { currentTodoUiState ->
                currentTodoUiState.copy(
                    isShownToast = false
                )
            }
        }
    }


    /**
     * 투두 리스트 업데이트
     **/
    private fun updateTodoList(todoDate: LocalDate) {
        /*        _todoUiState.update { currentTodoUiState ->
                    currentTodoUiState.copy(
                        currentDate = todoDate
                    )
                }*/


        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            todoDao.getTodoListWithDate(todoDate.dateToString()).collectLatest { todoList ->
                val updatedDeleteDatasSet = mutableSetOf<Int>()
                todoList.forEach {
                    if (it.isDone) {
                        updatedDeleteDatasSet.add(it.id)
                    }
                }
                _todoListUiState.update { currentTodoUiState ->
                    currentTodoUiState.copy(
                        todoList = todoList,
                        currentDate = todoDate,
                        deleteDataSet = updatedDeleteDatasSet
                    )
                }
            }
        }
        /*todoDao.getAllTodoList().collect { todoList ->
            val updatedDeleteDatasSet = mutableSetOf<Int>()
            todoList.forEach {
                if (it.isDone) {
                    updatedDeleteDatasSet.add(it.id)
                }
            }
            _todoUiState.update { currentTodoUiState ->
                currentTodoUiState.copy(
                    todoList = todoList,
                    deleteDataSet = updatedDeleteDatasSet
                )
            }
        }*/
    }

    private fun updateCurrentDate(currentDate: LocalDate) {
        updateTodoList(currentDate)
    }


    /**
     * 투두 데이터 업데이트
     **/
    private suspend fun updateTodoData(todoUiState: TodoUiState = _todoUiState.value) {
        todoDao.update(todoUiState.todoData)

        /*        _todoUiState.update { currentTodoUiState ->
                    val updatedTodoList = currentTodoUiState.todoList.toMutableList()
                    val index = updatedTodoList.indexOfFirst { it.id == _todoUiState.value.todoData.id }
                    if (index != -1) {
                        updatedTodoList[index] = _todoUiState.value.todoData
                    }
                    currentTodoUiState.copy(
                        todoList = updatedTodoList
                    )
                }*/
        //updateTodoList()
    }


    /**
     * 투두 데이터 삭제
     **/
    private fun deleteTodoData(id: Int = -1) {
        if (id != -1) {
            viewModelScope.launch {
                todoDao.delete(getTodoData(id))
            }
            _todoListUiState.value.deleteDataSet.remove(id)
        } else {
            viewModelScope.launch {
                _todoListUiState.value.deleteDataSet.forEach { itemId ->
                    todoDao.delete(getTodoData(itemId))
                }
            }
            //_todoUiState.value.deleteDataSet.clear()
        }

        /*_todoUiState.update { currentTodoUiState ->
            currentTodoUiState.copy(
                deleteDataSet = currentTodoUiState.deleteDataSet
            )
        }*/
    }

    private suspend fun getTodoData(id: Int): TodoData {
        return if (id == -1) {
            TodoData()
        } else {
            todoDao.getTodoData(id).first()
        }
    }
}