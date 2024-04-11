package com.example.hpcl.utils

import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor(private val maxRetries: Int) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0

        while (!response.isSuccessful && tryCount < maxRetries) {
            tryCount++
            request = request.newBuilder().build()
            response = chain.proceed(request)
        }

        return response
    }
}