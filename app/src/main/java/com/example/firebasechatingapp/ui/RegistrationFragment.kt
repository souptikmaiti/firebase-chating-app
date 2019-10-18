package com.example.firebasechatingapp.ui


import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.databinding.FragmentRegistrationBinding
import com.example.firebasechatingapp.viewmodel.UserListener
import com.example.firebasechatingapp.viewmodel.UserViewModel
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_registration.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

/**
 * A simple [Fragment] subclass.
 */
class RegistrationFragment : Fragment(), KodeinAware, UserListener {

    override val kodein: Kodein by kodein()
    private val factory:UserViewModelFactory by instance()
    private lateinit var viewModel:UserViewModel
    private var imageUri: Uri?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this,factory).get(UserViewModel::class.java)
        viewModel.userListener = this
        var binding:FragmentRegistrationBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_registration,container,false)
        binding.userViewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_register.setOnClickListener {
            viewModel.registerUser()
        }
        tv_log_in.setOnClickListener {
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.registrationFragment, true).build())
        }
    }

    override fun onRegister(msg: String) {
        viewModel.addUser()
        Snackbar.make(root_linear_layout,msg,Snackbar.LENGTH_LONG).also {snackbar ->
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
        }.show()
    }

    override fun onFailure(msg: String) {
        Snackbar.make(root_linear_layout,msg, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
        }.show()
    }

    override fun onSuccess(msg: String) {
        if(msg.equals("user info saved")){
            if(imageUri!=null){

            }
        }
        findNavController().navigate(R.id.action_registrationFragment_to_profileFragment, null,
            NavOptions.Builder().setPopUpTo(R.id.registrationFragment,true).build())
    }
}
