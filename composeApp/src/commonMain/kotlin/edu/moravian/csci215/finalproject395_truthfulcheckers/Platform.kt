package edu.moravian.csci215.finalproject395_truthfulcheckers

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform