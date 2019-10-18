package com.example.firebasechatingapp.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebasechatingapp.R
import com.example.firebasechatingapp.data.model.User
import com.example.firebasechatingapp.viewmodel.UserViewModel
import com.example.firebasechatingapp.viewmodel.UserViewModelFactory
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.GroupieViewHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_new_chat.*
import kotlinx.android.synthetic.main.user_row_item.view.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class NewChatFragment : Fragment(),KodeinAware {

    override val kodein: Kodein by kodein()
    private val factory: UserViewModelFactory by instance()
    private lateinit var viewModel: UserViewModel
    private val compositeDisposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this,factory).get(UserViewModel::class.java)
        return inflater.inflate(R.layout.fragment_new_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        val disposable = viewModel.getUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    for(u in it){
                        adapter.add(UserItem(u))
                    }
                    rv_users.adapter = adapter
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem
                        val bundle = bundleOf("selectedUser" to userItem.user)
                        findNavController().navigate(R.id.action_newChatFragment_to_chatingFragment,bundle,
                            NavOptions.Builder().setPopUpTo(R.id.newChatFragment,true).build())
                    }
                },{

                }
            )
        compositeDisposable.add(disposable)
    }

    class UserItem(val user:User): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.user_row_item
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if(user.userName!=null) {
                viewHolder.itemView.tv_username.text = user.userName
            }else{
                viewHolder.itemView.tv_username.text = user.email
            }
            if(user.imageLink!=null){
                Glide.with(viewHolder.itemView.context).load(user.imageLink).into(viewHolder.itemView.iv_user)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
