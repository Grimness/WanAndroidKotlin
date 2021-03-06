package com.ziqi.baselibrary.http.error

import org.json.JSONException
import retrofit2.HttpException

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.ziqi.baselibrary.util.LogUtil
import java.net.ConnectException

/**
 *    作者 : moziqi
 *    邮箱 : 709847739@qq.com
 *    时间   : 2020/4/26-14:38
 *    desc   :
 *    version: 1.0
 */
object ExceptionHandle {
    fun handleException(e: Throwable): ResponseThrowable {
        LogUtil.e("handleException", e)
        val ex: ResponseThrowable
        if (e is ResponseThrowable) {
            ex = e
        } else if (e is HttpException) {
            ex = ResponseThrowable(
                ERROR.HTTP_ERROR,
                e
            )
        } else if (e is JsonParseException
            || e is JSONException
            || e is ParseException
            || e is MalformedJsonException
        ) {
            ex = ResponseThrowable(
                ERROR.PARSE_ERROR,
                e
            )
        } else if (e is ConnectException) {
            ex = ResponseThrowable(
                ERROR.NETWORD_ERROR,
                e
            )
        } else if (e is javax.net.ssl.SSLException) {
            ex = ResponseThrowable(
                ERROR.SSL_ERROR,
                e
            )
        } else if (e is java.net.SocketTimeoutException || e is java.net.UnknownHostException) {
            ex = ResponseThrowable(
                ERROR.TIMEOUT_ERROR,
                e
            )
        } else {
            ex = if (!e.message.isNullOrEmpty()) ResponseThrowable(
                1000,
                e.message!!,
                e
            ) else ResponseThrowable(ERROR.UNKNOWN, e)
        }
        return ex
    }
}