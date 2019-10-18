package com.example.firebasechatingapp.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.viewmodel.UserViewModel
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

/**
 * A simple [Fragment] subclass.
 */
class SplashFragment : Fragment(),KodeinAware {
    override val kodein: Kodein by kodein()
    private val factory: UserViewModelFactory by instance()
    private lateinit var viewModel: UserViewModel
    private var mUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this,factory).get(UserViewModel::class.java)
        mUser = viewModel.getCurrentUser()
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(mUser == null){
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.splashFragment, true).build())
        }else{
            findNavController().navigate(R.id.action_splashFragment_to_profileFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.splashFragment, true).build())
        }
    }


}
