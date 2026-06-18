package com.example.myapplication

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?
): Interceptor {
    override fun intercept(chain : Interceptor.Chain) : Response {
        val token = tokenProvider()
        // If no token (user not logged in), send request as-is
        // MangaDex allows unauthenticated reads for public manga
        val request=if (token!=null){
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else{
            chain.request()
        }
        return chain.proceed(request)
    }

}