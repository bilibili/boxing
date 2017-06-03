/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bilibili.boxing.utils

import android.os.Handler
import android.os.Looper

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * @author ChenSL
 */
class BoxingExecutor private constructor() {

    private var mExecutorService: ExecutorService? = null

    fun runWorker(runnable: Runnable) {
        ensureWorkerHandlerNotNull()
        try {
            mExecutorService!!.execute(runnable)
        } catch (e: Exception) {
            BoxingLog.d("runnable stop running unexpected. " + e.message)
        }

    }

    fun runWorker(callable: Callable<Boolean>): FutureTask<Boolean>? {
        ensureWorkerHandlerNotNull()
        var task: FutureTask<Boolean>? = null
        try {
            task = FutureTask(callable)
            mExecutorService!!.submit(task)
            return task
        } catch (e: Exception) {
            BoxingLog.d("callable stop running unexpected. " + e.message)
        }

        return task
    }

    fun runUI(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
            return
        }
        val handler = ensureUiHandlerNotNull()
        try {
            handler.post(runnable)
        } catch (e: Exception) {
            BoxingLog.d("update UI task fail. " + e.message)
        }

    }

    private fun ensureWorkerHandlerNotNull() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newCachedThreadPool()
        }
    }

    private fun ensureUiHandlerNotNull(): Handler {
        return Handler(Looper.getMainLooper())
    }

    companion object {
        val instance = BoxingExecutor()
    }

}
