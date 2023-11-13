package com.example.todolist.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todoData: TodoData)

    @Update
    suspend fun update(todoData: TodoData)

    @Delete
    suspend fun delete(todoData: TodoData)

    @Query("SELECT * FROM TodoData")
    fun getAllTodoList(): Flow<List<TodoData>>

    @Query("SELECT * FROM TodoData WHERE id = :id")
    fun getTodoData(id: Int): Flow<TodoData>

    @Query("SELECT * FROM TodoData WHERE todoDate = :todoDate")
    fun getTodoListWithDate(todoDate: String): Flow<List<TodoData>>
}