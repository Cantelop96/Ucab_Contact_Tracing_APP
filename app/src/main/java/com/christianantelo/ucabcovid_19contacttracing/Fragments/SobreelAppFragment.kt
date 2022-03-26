package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.R
import kotlinx.android.synthetic.main.fragment_main__view.*
import kotlinx.android.synthetic.main.fragment_sobreel_app.*

class SobreelAppFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sobreel_app, container, false)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object:OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_sobreelAppFragment_to_main_View)
            }
        })

        btn_return_to_main_con_sintomas.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_sobreelAppFragment_to_main_View)
        }
    }
    }
