package com.xheng.mydaygram.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*

class NoteWork(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {


    override fun doWork(): Result {
        val timer = Timer()
        val task = object: TimerTask() {
            override fun run() {
                NoteTask(context).notifyMessage()
            }

        }
        timer.scheduleAtFixedRate(task, 0, 2000)


        return Result.success()
    }


}