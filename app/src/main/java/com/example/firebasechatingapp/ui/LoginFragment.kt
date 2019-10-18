package com.example.firebasechatingapp.ui


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.databinding.FragmentLoginBinding
import com.example.firebasechatingapp.viewmodel.UserListener
import com.example.firebasechatingapp.viewmodel.UserViewModel
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class LoginFragment : Fragment(), KodeinAware, UserListener {
    override val kodein: Kodein by kodein()
    private val factory: UserViewModelFactory by instance()

    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this,factory).get(UserViewModel::class.java)
        viewModel.userListener = this
        var binding: FragmentLoginBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_login,container,false)
        binding.userViewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_log_in.setOnClickListener {
            viewModel.logInUser()
        }

        tv_sign_up.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.loginFragment,true).build())
        }
    }

    override fun onSuccess(msg: String) {
        Snackbar.make(root_linear_layout,msg,Snackbar.LENGTH_LONG).also {snackbar ->
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
        }.show()
        findNavController().navigate(R.id.action_loginFragment_to_profileFragment,
            null,NavOptions.Builder().setPopUpTo(R.id.loginFragment,true).build())
    }

    override fun onFailure(msg: String) {
        Snackbar.make(root_linear_layout,msg,Snackbar.LENGTH_LONG).also {snackbar ->
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
        }.show()
    }

    override fun onRegister(msg: String) {

    }
}
