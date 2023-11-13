package com.example.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 투두 데이터
 **/
@Entity
data class TodoData(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,   //아이디
    var title: String = "",     //제목
    var description: String = "",   //내용
    var todoDate: LocalDate = LocalDate.now(),  //일정 날짜
    var todoTime: LocalTime = LocalTime.now(),  //일정 시간
    var writtenTime: LocalDateTime = LocalDateTime.now(),   //작성한 시간
    var isNotification: Boolean = false,    //알림 여부
    var isDone: Boolean = false     //완료 여부
)
