package com.example.hpcl.utils

abstract class CallBack<T> {
    abstract fun onSuccess(t: T?)
    open fun onError(error: String?) {}
}




