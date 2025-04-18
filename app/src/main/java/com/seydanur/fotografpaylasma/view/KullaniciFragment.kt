package com.seydanur.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.seydanur.fotografpaylasma.databinding.FragmentKullaniciBinding


class KullaniciFragment : Fragment() {
    private var _binding: FragmentKullaniciBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKullaniciBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitButton.setOnClickListener { kayitOl(it) }
        binding.girisButton.setOnClickListener { girisYap(it) }
        val current_user=auth.currentUser
        if(current_user!=null){
            //kullalnici giris var,
            val action=KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)

        }
    }
    fun kayitOl(view:View){
        val email=binding.emailText.text.toString()
        val password=binding.passwordText.text.toString()
        if(email.isNotEmpty()&& password.isNotEmpty()){

            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
                if(task.isSuccessful){
                    //kullanici olusturuldu
                    val action=KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
                }
            }.addOnFailureListener { exception->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()

            }
            }
    }
    fun girisYap(view:View){
        val email=binding.emailText.text.toString()
        val password=binding.passwordText.text.toString()
        if(email.isNotEmpty()&& password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val action=KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener { exception->Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }



    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}