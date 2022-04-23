package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.election.repository.ElectionRepository
import com.example.android.politicalpreparedness.network.CivicsApi
import com.google.android.material.snackbar.Snackbar


class ElectionsFragment: Fragment() {

    private val viewModel: ElectionsViewModel by lazy {
        ViewModelProvider(this,
            ElectionsViewModelFactory(
                ElectionRepository(
                    ElectionDatabase.getInstance(requireContext()).electionDao,
                    CivicsApi.retrofitService)
            )).get(ElectionsViewModel::class.java)
    }

    private lateinit var binding: FragmentElectionBinding

    private lateinit var upcomingElectionsAdapter: ElectionListAdapter
    private lateinit var savedElectionsAdapter: ElectionListAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.lifecycleOwner = this
        binding.electionViewModel = viewModel


        upcomingElectionsAdapter = ElectionListAdapter(ElectionListener {
            viewModel.navigateTo(it)
        })
        savedElectionsAdapter = ElectionListAdapter(ElectionListener {
            viewModel.navigateTo(it)
        })


        binding.upcomingElectionsRV.adapter = upcomingElectionsAdapter
        binding.savedElectionsRV.adapter = savedElectionsAdapter


        observeViewModel()

        return binding.root
    }



    private fun observeViewModel(){
        viewModel.upcomingElections.observe(viewLifecycleOwner, Observer {
            upcomingElectionsAdapter.submitList(it)
        })

        viewModel.savedElections.observe(viewLifecycleOwner, Observer {
            savedElectionsAdapter.submitList(it)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })

        viewModel.navigateTo.observe(viewLifecycleOwner, Observer {
            it?.let {
                viewModel.navigateToVoterInfoCompleted()
                findNavController().navigate(it)
            }
        })
    }


}