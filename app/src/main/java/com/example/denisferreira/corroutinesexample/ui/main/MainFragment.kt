package com.example.denisferreira.corroutinesexample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.denisferreira.corroutinesexample.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private val repository = FilmeRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]
        viewModel.filmes.observe(viewLifecycleOwner) { filmes ->
            textViewFilmes.text = filmes[0].nome
        }
        viewModel.ultimosFilmes.observe(viewLifecycleOwner) {
            textViewUltimosFilmes.text = it.nome
        }
//        viewModel.updateFilmes()
        viewModel.updateCorroutinesFilmes()
    }
}