package com.example.firebasechatingapp.ui


import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.data.model.Chat
import com.example.firebasechatingapp.data.model.User
import com.example.firebasechatingapp.viewmodel.UserListener
import com.example.firebasechatingapp.viewmodel.UserViewModel
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.latest_message_item.view.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ProfileFragment : Fragment(), KodeinAware, UserListener {

    override val kodein: Kodein by kodein()
    private val factory: UserViewModelFactory by instance()
    private lateinit var viewModel: UserViewModel
    private var selectedUser:User?=null
    companion object {
        var currentUser: User?=null
    }
    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this,factory).get(UserViewModel::class.java)
        viewModel.userListener = this
        getCurrentUserInfo()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    private fun getCurrentUserInfo(){
        val disposable2 = viewModel.getUserInfo(viewModel.getCurrentUser()!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    currentUser = it
                },{

                }
            )
        compositeDisposable.add(disposable2)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLatetMessages()
    }

    private fun setUpLatetMessages() {
        val disposable2 = viewModel.getLatestMessages(viewModel.getCurrentUser()!!.uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val adapter = GroupAdapter<GroupieViewHolder>()
                    for(ch in it){

                        adapter.add(LatestMessageItem(ch))
                        rv_latest_chats.adapter = adapter
                        adapter.setOnItemClickListener { item, view ->
                            val latestMessageItem = item as LatestMessageItem
                            val bundle = bundleOf("selectedUser" to latestMessageItem.chatPartner)
                            findNavController().navigate(R.id.action_profileFragment_to_chatingFragment,bundle)
                        }
                    }

                },{

                }
            )

    }

    inner class LatestMessageItem(val chat:Chat) : Item<GroupieViewHolder>(){
        var chatPartner:User?=null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.tv_late_msg.text = chat.text
            var partnerId = chat.toId
            if(chat.toId ==viewModel!!.getCurrentUser()!!.uid) partnerId = chat.fromId
            val disposable3 = viewModel.getUserInfo(partnerId!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        chatPartner = it
                        viewHolder.itemView.tv_username.text = it.userName
                        Glide.with(viewHolder.itemView.context).load(it.imageLink).into(viewHolder.itemView.iv_profile)
                    },{

                    }
                )
            compositeDisposable.add(disposable3)
        }

        override fun getLayout(): Int {
            return R.layout.latest_message_item
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.pfrofile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_log_out ->{
                viewModel.signOutUser()
            }
            R.id.menu_update ->{
                findNavController().navigate(R.id.action_profileFragment_to_updateFragment)
            }
            R.id.menu_new_chat ->{
                findNavController().navigate(R.id.action_profileFragment_to_newChatFragment)
            }
        }
        return true
    }

    override fun onFailure(msg: String) {
        Snackbar.make(root_relative_layout,msg, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
        }.show()
    }

    override fun onSuccess(msg: String) {
        Snackbar.make(root_relative_layout,msg, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("OK"){
                snackbar.dismiss()
            }
        }.show()
        if(msg.equals("Log Out Successfully")){
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.profileFragment, true).build())
        }
    }

    override fun onRegister(msg: String) {

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
