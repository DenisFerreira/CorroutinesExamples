package com.example.denisferreira.corroutinesexample.ui.main

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class FilmeRepository {

    val list = listOf(
        Filme(1, "Titulo 1"),
        Filme(2, "Titulo 2")
    )
    /*
        Exemplo de como seria utilizado em Retrofit no qual os valores são retornados no callback
    */
    fun getFilmes(callback: (List<Filme>) -> Unit) {

        thread {
            Thread.sleep(3000)
            callback.invoke(list)
        }
    }

    /*
        Exemplo de função suspensa
        Rodando no contexto de entrada e saída de dados

     */
    suspend fun getFilmesCorroutines() : List<Filme> {
        return withContext(Dispatchers.IO) {
            delay(3000)
            list
        }
    }

    val ultimosFilmes : Flow<Filme> = flow {
        var i = 0;
        while(true){
            i++
            val ultimoFilme = Filme(i, "Titulo #${i}")
            emit(ultimoFilme)
            delay(3000)
        }
    }
}
