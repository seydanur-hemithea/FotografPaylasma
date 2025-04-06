package com.seydanur.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.seydanur.fotografpaylasma.R
import com.seydanur.fotografpaylasma.adapter.PostAdapter
import com.seydanur.fotografpaylasma.databinding.FragmentFeedBinding
import com.seydanur.fotografpaylasma.model.Posts


class FeedFragment : Fragment(),PopupMenu.OnMenuItemClickListener {

    private var _binding: FragmentFeedBinding? = null

    private val binding get() = _binding!!
    private lateinit var popup:PopupMenu
    private lateinit var auth:FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val postList :ArrayList<Posts> = arrayListOf()
    private var adapter:PostAdapter?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth= Firebase.auth
        db=Firebase.firestore


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton3.setOnClickListener { floatingButtonTiklandi(it) }
        popup=PopupMenu(requireContext(),binding.floatingActionButton3)
        val inflater=popup.menuInflater
        inflater.inflate(R.menu.mypupupmenu,popup.menu)
        popup.setOnMenuItemClickListener(this)
        firestoredanVerileriAl()
        adapter=PostAdapter(postList)
        binding.feedRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter=adapter
    }
    private fun firestoredanVerileriAl(){
        db.collection("posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener{ value, error->
            if(error!=null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
//addsnapshotlistener sürekli sunucya istek gondercek chat programı gibi bir sey yapıldıgında sürekli yenilemis olucak

            }else{
                if(value!=null){
                    if(!value.isEmpty){
                        //bos degilse
                        postList.clear()
                        val documents=value.documents
                        for(document in documents){
                            val comment=document.get("comment")as String //comment any oalrak gelecek onu strring olank casting yapıyorux
                            val email=document.get("email") as String
                            val downloadUrl=document.get("downloadURL") as String

                            val post= Posts(email,comment,downloadUrl)
                            //key leri aynen yazıp tüm value leri gösterebilriz
                            postList.add(post)

                        }
                        adapter?.notifyDataSetChanged()//yeni veriler geldi adapteri yeniden olustur

                    }
                }
            }
        }
    }
    fun floatingButtonTiklandi(view:View){

        popup.show()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
       if (item?.itemId== R.id.yuklemeItem){
           val action=FeedFragmentDirections.actionFeedFragmentToUploadFragment()
           Navigation.findNavController(requireView()).navigate(action)

       }else if(item?.itemId== R.id.cikisItem){
           auth.signOut()
           val action=FeedFragmentDirections.actionFeedFragmentToKullaniciFragment()
           Navigation.findNavController(requireView()).navigate(action)

       }
        return true  }
}


