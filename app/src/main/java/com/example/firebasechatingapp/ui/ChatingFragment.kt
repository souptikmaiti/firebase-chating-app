package com.example.firebasechatingapp.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.data.model.Chat
import com.example.firebasechatingapp.data.model.User
import com.example.firebasechatingapp.databinding.FragmentChatingBinding
import com.example.firebasechatingapp.viewmodel.ChatListener
import com.example.firebasechatingapp.viewmodel.ChatViewModel
import com.example.firebasechatingapp.viewmodel.ChatViewModelFactory
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_chating.*
import kotlinx.android.synthetic.main.to_chat_item.view.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class ChatingFragment : Fragment(), KodeinAware, ChatListener {
    override val kodein: Kodein by kodein()
    private val factory:ChatViewModelFactory by instance()
    lateinit var viewModel:ChatViewModel
    private var selectedUser: User?=null
    private var currentUser:User?=null
    private val compositeDisposable = CompositeDisposable()
    //private var adapter: GroupAdapter<GroupieViewHolder> ?=null
    private var adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this,factory).get(ChatViewModel::class.java)
        viewModel.chatListener = this
        var binding:FragmentChatingBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_chating,container,false)
        binding.chatViewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            selectedUser = it.getParcelable("selectedUser")
        }

        viewModel.toId = selectedUser?.id
        if(viewModel.fromId!=null && viewModel.toId !=null){
            setUpChatAdded()
        }
        btn_send.setOnClickListener {
            if(viewModel.fromId !=null && viewModel.toId !=null){
                if( viewModel.msg !=null && viewModel.msg!!.trim()!!.isNotEmpty()){
                    viewModel.addChat()
                }
            }
        }
    }

    private fun setUpChatAdded() {
        val disposable = viewModel.getChatAdded()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    currentUser = ProfileFragment.currentUser

                    if(it.fromId == viewModel.fromId){
                        adapter!!.add(ChatItemFrom(it,currentUser?.imageLink))
                    }else{
                        adapter!!.add(ChatItemTo(it,selectedUser?.imageLink))
                    }

                    rv_messages.adapter = adapter
                },{

                }
            )
        compositeDisposable.add(disposable)
    }

    /*private fun setUpChat() {  // don't use as entire list gets updated each time we send a message
        val disposable = viewModel.getChats()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    adapter = GroupAdapter<GroupieViewHolder>()
                    currentUser = ProfileFragment.currentUser
                    for(ch in it){
                        if(ch.fromId == viewModel.fromId){
                            adapter!!.add(ChatItemFrom(ch,currentUser?.imageLink))
                        }else{
                            adapter!!.add(ChatItemTo(ch,selectedUser?.imageLink))
                        }
                    }
                    rv_messages.adapter = adapter
                },{

                }
            )
        compositeDisposable.add(disposable)
    }*/

    class ChatItemFrom(val chat: Chat,val imageLink:String?): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.from_chat_item
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.tv_chat.text = chat.text
            if(imageLink!=null) Glide.with(viewHolder.itemView.context).load(imageLink).into(viewHolder.itemView.iv_photo)
        }
    }

    class ChatItemTo(val chat: Chat,val imageLink:String?): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.to_chat_item
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.tv_chat.text = chat.text
            if(imageLink!=null) Glide.with(viewHolder.itemView.context).load(imageLink).into(viewHolder.itemView.iv_photo)
        }
    }

    override fun onSuccess(msg: String) {
        et_msg.text.clear()
        adapter?.let {
            rv_messages.scrollToPosition(it.itemCount - 1)
        }
    }

    override fun onFailure(msg: String) {
        Toast.makeText(this.context,msg,Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
