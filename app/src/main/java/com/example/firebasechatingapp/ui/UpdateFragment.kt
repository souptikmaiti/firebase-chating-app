package com.example.firebasechatingapp.ui


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.databinding.FragmentUpdateBinding
import com.example.firebasechatingapp.viewmodel.UserListener
import com.example.firebasechatingapp.viewmodel.UserViewModel
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import kotlinx.android.synthetic.main.fragment_update.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance


class UpdateFragment : Fragment(), KodeinAware, UserListener {
    override val kodein: Kodein by kodein()
    private val factory: UserViewModelFactory by instance()
    private lateinit var viewModel: UserViewModel
    private var imageUri: Uri?=null

    companion object{
        private val IMAGE_REQ = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this,factory).get(UserViewModel::class.java)
        viewModel.userListener = this
        var binding: FragmentUpdateBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_update,container,false)
        binding.userViewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,IMAGE_REQ)
        }
        btn_update.setOnClickListener {
            if(imageUri!= null){
                viewModel.addUserPhoto(imageUri!!,getExtension(imageUri!!)!!)
            }else{
                if(!viewModel.email.isNullOrBlank() || !viewModel.userName.isNullOrBlank()){
                    viewModel.updateUser()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMAGE_REQ && resultCode== Activity.RESULT_OK && data!=null && data.data!=null){
            imageUri = data.data
            Glide.with(this).load(imageUri).into(iv_profile)
        }
    }

    private fun getExtension(uri: Uri): String? {
        val cr = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr?.getType(uri))
    }

    override fun onSuccess(msg: String) {
        if(msg.equals("image uploaded successfully")){
            if(!viewModel.email.isNullOrBlank() || !viewModel.userName.isNullOrBlank()){
                viewModel.updateUser()
            }
        }
    }

    override fun onRegister(msg: String) {

    }

    override fun onFailure(msg: String) {

    }
}
