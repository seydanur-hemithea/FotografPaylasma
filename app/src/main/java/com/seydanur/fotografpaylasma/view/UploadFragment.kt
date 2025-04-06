package com.seydanur.fotografpaylasma.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.seydanur.fotografpaylasma.databinding.FragmentUploadBinding
import java.util.UUID


class UploadFragment : Fragment() {


    private var _binding: FragmentUploadBinding? = null

    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher:ActivityResultLauncher<String>
    var secilenGorsel: Uri?=null
    var secilenBitmap: Bitmap?=null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage:FirebaseStorage
    private lateinit var db:FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db=Firebase.firestore


        registerLaunchers()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView3.setOnClickListener { gorselSec(it) }
        binding.paylasButton.setOnClickListener { yukleTiklandi(it) }


    }
    fun yukleTiklandi(view:View) {
        val uuid= UUID.randomUUID()
        val gorselAdi="${uuid}.jpg"

        val reference = storage.reference
        val gorselRefence =
            reference.child("images").child("gorselAdi")//storage da klasor olustulur
        if (secilenGorsel != null) {
            gorselRefence.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask ->
                //url yi alma islemi
                gorselRefence.downloadUrl.addOnSuccessListener { uri->
                    if(auth.currentUser!=null){


                    val downloadURL=uri.toString()
                    //println(downloadURL)
                    //veritabanına kayıt yapmamak gerekiyor. storage da ilk olarak url olusması için yükleme yapılmalı
                    //daha sonra bu url yi veritabanına kaydetmemiz gerek
                    //firestore database bir veri tabanı storage ise bir depolama birimi onedrive gibi
                    val postMap=hashMapOf<String,Any>()
                    postMap.put("downloadUrl",downloadURL)
                    postMap.put("email",auth.currentUser!!.email.toString())
                    postMap.put("comment",binding.commentText.text.toString())
                    postMap.put("date",Timestamp.now())
                    db.collection("posts").add(postMap).addOnSuccessListener { documentReference->
                        val action=UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                        Navigation.findNavController(view).navigate(action)
                    }
                        .addOnFailureListener { exception->Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show() }
                    }

                }

            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG)
                    .show()

            }


        }
    }


        fun gorselSec(view:View){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_MEDIA_IMAGES)) {
                        Snackbar.make(
                            view,
                            "Galeriye gitmek için izin vermeniz gerekiyor",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("izin ver", View.OnClickListener {
                            //izin isteme
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()

                    } else{//izin iste
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                     }}else{//izin var galeriye gitme kodu
                    val intentToGalery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGalery)

                    }}else {
                //read external storage

                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Galeriye gitmek için izin vermeniz gerekiyor",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("izin ver", View.OnClickListener {
                            //izin isteme
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()

                    } else {//izin iste
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {//izin var galeriye gitme kodu
                    val intentToGalery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGalery)

                }

            }}


    private fun registerLaunchers() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data//secilen data
                    if (intentFromResult != null) {
                        secilenGorsel = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    secilenGorsel!!
                                )
                                secilenBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView3.setImageBitmap(secilenBitmap)
                            } else {
                                secilenBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    secilenGorsel
                                )
                                binding.imageView3.setImageBitmap(secilenBitmap)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //izin verildi
                    val intentToGalery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGalery)
                } else {
                    //kullanıcı izni reddetti
                    Toast.makeText(
                        requireContext(),
                        "izni reddettiniz,izne ihtiyacımız var",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }}