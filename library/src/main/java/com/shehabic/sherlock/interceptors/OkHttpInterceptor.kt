package com.shehabic.sherlock.interceptors

import com.shehabic.sherlock.NetworkSherlock
import com.shehabic.sherlock.db.NetworkRequests
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class SherlockOkHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        val srcRequest = chain?.request()!!
        val request = NetworkRequests(0, NetworkSherlock.getInstance().getSessionId())
        request.method = srcRequest.method()
        request.requestUrl = srcRequest.url().toString()
        request.requestHeaders = srcRequest.headers().toString()
        request.requestBody = srcRequest.body().toString()
        request.requestContentType = srcRequest.body()?.contentType()?.toString()
        NetworkSherlock.getInstance().startRequest()
        try {
            val response = chain.proceed(srcRequest)
            request.responseBody = response.body()?.string()
            request.statusCode = response.code()
            request.responseHeaders = response.headers().toString()
            request.responseLength = response.body()?.contentLength() ?: 0L
            request.requestStartTime = response.sentRequestAtMillis()
            request.requestHeaders = response.request().headers().toString()
            request.responseTime = response.receivedResponseAtMillis() - request.requestStartTime
            request.responseMessage = response.message()?.toString()
            request.responseContentType = response.body()?.contentType()?.toString()
            request.isRedirect = response.isRedirect
            request.protocol = response.protocol().toString()

            NetworkSherlock.getInstance().addRequest(request)
            NetworkSherlock.getInstance().endRequest()

            return response
        } catch (e: IOException) {
            request.responseError = e.message
            NetworkSherlock.getInstance().addRequest(request)
            NetworkSherlock.getInstance().endRequest()
            throw e
        }
    }
}