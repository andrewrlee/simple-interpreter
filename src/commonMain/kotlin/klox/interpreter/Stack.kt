package klox.interpreter

typealias Stack<T> = MutableList<T>

fun <T> Stack<T>.push(item: T) = this.add(this.size, item)
fun <T> Stack<T>.pop(): T = this.removeAt(this.size - 1)
fun <T> Stack<T>.peek(): T = this[this.size - 1]

fun <T> stackOf() = mutableListOf<T>()