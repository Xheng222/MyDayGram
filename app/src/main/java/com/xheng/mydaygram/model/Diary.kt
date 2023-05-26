package com.xheng.mydaygram.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import org.litepal.crud.LitePalSupport
import java.time.DayOfWeek
import java.time.Month
import java.time.Year

//日记类
class Diary() : LitePalSupport() {

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var week: Int = 0
    //日记
    private var diary: String? = null

    constructor(year: Int, month: Int, day: Int, week: Int, diary: String?) : this() {
        this.day = day
        this.month = month
        this.year = year
        this.week = week
        this.diary = diary
    }
    /**
     * 获取各种日记信息
     */
    fun getYear(): Int = year

    fun getMonth(): Int = month

    fun getDay(): Int = day

    fun getWeek(): Int = week

    fun getDiary(): String? = diary

    //设置日记的内容
    fun setDiary(diary: String?) {
        this.diary = diary
    }

}