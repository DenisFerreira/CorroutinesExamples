package com.example.denisferreira.corroutinesexample.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(val repository: FilmeRepository) : ViewModel() {
    // TODO: Implement the ViewModel

    val filmes = MutableLiveData<List<Filme>>()
    val ultimosFilmes = MutableLiveData<Filme>()
    val falhaEnvio = MutableLiveData<String>()


    /*
        Execução de forma não sequencial, o retorno da função depende da ativação do callback
     */
    fun updateFilmes() {
        repository.getFilmes { lista ->
            filmes.postValue(lista)
        }
    }

    /*
        As funções suspensas precisam ser executadas dentro de um escopo
        Desta forma elas são chamadas de forma sequencial apesar de ser executadas em threads
        separadas.
     */
    fun updateCorroutinesFilmes() {
        viewModelScope.launch {
            val list = repository.getFilmesCorroutines()
            filmes.value = list
        }
//        Ou
//        ***
//        CoroutineScope(Dispatchers.Main).launch {
//
//        }
    }

    /*
        Implementação de um fluxo contínuo de dados
     */
    init {
        viewModelScope.launch {
            repository.ultimosFilmes
                .catch { exception -> falhaEnvio.value = exception.localizedMessage }
                .collect {
                    ultimosFilmes.value = it
                }

        }
    }

}

class MainViewModelFactory(private val repository: FilmeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}