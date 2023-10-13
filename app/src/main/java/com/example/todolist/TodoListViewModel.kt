package com.example.todolist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.model.TodoDao
import com.example.todolist.model.TodoData
import com.example.todolist.model.TodoDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoListViewModel(application: Application) : AndroidViewModel(application) {
    private var todoDb: TodoDatabase = TodoDatabase.getInstance(application)
    private var todoDao: TodoDao = todoDb.todoDao()
    private var _todoList = MutableStateFlow<List<TodoData>>(emptyList())
    private var _todoData = MutableStateFlow(TodoData())

    val deleteItemsSet: MutableSet<Int> = mutableSetOf()
    val todoData: StateFlow<TodoData> = _todoData.asStateFlow()
    val todoList: StateFlow<List<TodoData>> = _todoList.asStateFlow()

    init {
        viewModelScope.launch {
            updateTodoList()
        }
    }

    fun setTodoData(id: Int) {
        if (id == -1) {
            _todoData.value = TodoData()
        } else {
            viewModelScope.launch {
                _todoData.value = getTodoItem(id)
            }
        }
    }


    fun modifyTodoData(updateFlag: Boolean = false, todoData: TodoData) {
        viewModelScope.launch {
            if (updateFlag) updateTodoData() else insertTodoData()
        }
    }

    private suspend fun insertTodoData() {
        todoDao.insert(_todoData.value)
        updateTodoList()
    }

    fun updateTodoTitle(title: String) {
        _todoData.update { currentTodoData ->
            currentTodoData.copy(
                title = title
            )
        }
        Log.d("confirm updateTodoTitle : ", "$title, ${_todoData.value}")
    }

    fun updateTodoDescription(description: String) {
        _todoData.update { currentTodoData ->
            currentTodoData.copy(
                description = description
            )
        }
        Log.d("confirm updateTodoTitle : ", "$description, ${_todoData.value}")
    }

    fun updateTodoIsDone(id: Int) {
        viewModelScope.launch {
            _todoData.value = getTodoItem(id)

            _todoData.update { currentTodoData ->
                currentTodoData.copy(
                    isDone = !currentTodoData.isDone
                )
            }

            updateTodoData()

            if (_todoData.value.isDone) deleteItemsSet.add(id)
            else deleteItemsSet.remove(id)
        }
    }


    private suspend fun updateTodoList() {
        _todoList.value = todoDao.getAllTodoList().first()
        _todoList.value.forEach {
            if (it.isDone)
                deleteItemsSet.add(it.id)
        }
    }

    private suspend fun updateTodoData() {
        todoDao.update(_todoData.value)
        updateTodoList()
    }

    fun deleteTodoItem(id: Int = -1) {
        viewModelScope.launch {
            if (id != -1) {
                todoDao.delete(getTodoItem(id))
                if (deleteItemsSet.contains(id))
                    deleteItemsSet.remove(id)
            } else {
                deleteItemsSet.forEach {
                    todoDao.delete(getTodoItem(it))
                }
                deleteItemsSet.clear()
            }
            updateTodoList()
        }
    }

    private suspend fun getTodoItem(id: Int): TodoData {
        return if (id == -1) {
            TodoData()
        } else {
            todoDao.getTodoItem(id).first()
        }
    }
}