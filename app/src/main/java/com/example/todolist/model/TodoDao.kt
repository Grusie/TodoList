package com.example.todolist.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(todoData: TodoData)

    @Update
    suspend fun update(todoData: TodoData)

    @Delete
    suspend fun delete(todoData: TodoData)

    @Query("SELECT * FROM TodoData")
    fun getAllTodoList() : Flow<List<TodoData>>

    @Query("SELECT * FROM TodoData WHERE id = :id")
    fun getTodoItem(id: Int) : Flow<TodoData>
}