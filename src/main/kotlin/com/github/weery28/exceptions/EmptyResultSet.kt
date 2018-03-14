package com.github.weery28.exceptions

class EmptyResultSet : NullPointerException {

    constructor() : super()
    constructor(message: String?) : super(message)
}