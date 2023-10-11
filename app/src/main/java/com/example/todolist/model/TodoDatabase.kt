package com.example.todolist.model

import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TodoData::class], version = 1)
abstract class TodoDatabase: RoomDatabase(){
    abstract fun todoDao(): TodoDao

    companion object {
        private var INSTANCE: TodoDatabase? = null
        @Synchronized
        fun getInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}