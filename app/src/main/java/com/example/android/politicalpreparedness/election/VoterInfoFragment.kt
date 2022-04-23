package com.example.android.politicalpreparedness.election

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.election.repository.ElectionRepository
import com.example.android.politicalpreparedness.network.CivicsApi
import com.google.android.material.snackbar.Snackbar

class VoterInfoFragment : Fragment() {

    private val viewModel: VoterInfoViewModel by lazy {
        ViewModelProvider(this,
            VoterInfoViewModelFactory(
                ElectionRepository(
                    ElectionDatabase.getInstance(requireContext()).electionDao,
                    CivicsApi.retrofitService)
            )).get(VoterInfoViewModel::class.java)
    }

    private lateinit var binding: FragmentVoterInfoBinding


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel



        arguments?.let {
            val args = VoterInfoFragmentArgs.fromBundle(it)

            if (args.argElectionId == 0) {
                showErrorDialog()
                return@let
            }

            viewModel.getVoterInfo(args.argElectionId, args.argDivision)

        }

        binding.followElectionBtn.setOnClickListener {
            viewModel.onSaveElection()
        }

        observeViewModel()

        return binding.root
    }


    private fun showErrorDialog() {
        AlertDialog.Builder(context)
            .setTitle(R.string.error_title)
            .setCancelable(false)
            .setMessage(R.string.error_message)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                this.findNavController().popBackStack()
            }.show()
    }

    private fun observeViewModel() {

        viewModel.savedButtonState.observe(viewLifecycleOwner, Observer {
                state ->
            if (state) {
                binding.followElectionBtn.text = getString(R.string.unfollow_election)
            } else {
                binding.followElectionBtn.text = getString(R.string.follow_election)
            }
        })

        viewModel.openLinkAction.observe(viewLifecycleOwner, Observer { url ->
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
    }
}