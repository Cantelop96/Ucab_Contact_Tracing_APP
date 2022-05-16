package com.christianantelo.ucabcovid_19contacttracing.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.christianantelo.ucabcovid_19contacttracing.R
import com.christianantelo.ucabcovid_19contacttracing.storage.ContactTracingDatabase
import kotlinx.android.synthetic.main.fragment_borrar_todo_confirmation.*

class BorrarTodoConfirmationFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_borrar_todo_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,object:
            OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_settingsFragment2_to_main_View)
            }
        })
        btn_delete_all_info_confirmado.setOnClickListener {
            deleteAllInfo()
        }
    }

    private val db by lazy { ContactTracingDatabase.invoke(this).getContactTracingDao()}
    fun deleteAllInfo() = db.clear()
}