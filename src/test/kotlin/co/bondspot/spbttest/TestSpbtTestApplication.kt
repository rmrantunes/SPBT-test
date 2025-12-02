package co.bondspot.spbttest

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<SpbtTestApplication>().with(TestcontainersConfiguration::class).run(*args)
}
