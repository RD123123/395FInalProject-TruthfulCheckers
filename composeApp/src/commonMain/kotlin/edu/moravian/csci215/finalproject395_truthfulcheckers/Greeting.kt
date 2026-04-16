package edu.moravian.csci215.finalproject395_truthfulcheckers

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}