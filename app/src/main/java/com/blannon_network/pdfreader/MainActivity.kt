package com.blannon_network.pdfreader

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.blannon_network.Pdffile
import com.blannon_network.pdfreader.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.github.muddz.styleabletoast.StyleableToast

class MainActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityMainBinding
    private var pdfFileUri: Uri? = null
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        initClickListeners()


    }

    private  fun init(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        storageReference = FirebaseStorage.getInstance().reference.child("pdfs")
        databaseReference = FirebaseDatabase.getInstance().reference.child("pdfs")

    }
    private  fun initClickListeners(){

        binding.selectPdfBtn.setOnClickListener{
            launcher.launch("application/pdf")
        }

        binding.uploadBtn.setOnClickListener{
            if (pdfFileUri != null){
                uploadPdfFileToFirebase()
            } else{
                StyleableToast.makeText(this, "Please select pdf first", R.style.Customtoast).show()
            }
        }

    }
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()){ Uri ->
        pdfFileUri = Uri
        val fileName = Uri?.let { DocumentFile.fromSingleUri(this, it)?.name }
        binding.NoSelect.text = fileName.toString()
    }

    private  fun uploadPdfFileToFirebase(){
        val fileName = binding.NoSelect.text.toString()
        val StorageRef = storageReference.child("${System.currentTimeMillis()}/$fileName")


        pdfFileUri?.let { uri ->
            StorageRef.putFile(uri).addOnSuccessListener {
                StorageRef.downloadUrl.addOnSuccessListener { downloadUri ->

                    val pdffile = Pdffile(fileName, downloadUri.toString())
                    databaseReference.push().key?.let { pushkey ->
                        databaseReference.child(pushkey).setValue(pdffile)
                            .addOnSuccessListener {
                                pdfFileUri = null
                                binding.NoSelect.text =
                                    resources.getString(R.string.no_file_selected_yet)
                                StyleableToast.makeText(this, "Uploaded Successfully ", R.style.Customtoast).show()
                            }.addOnFailureListener{err ->
                                StyleableToast.makeText(this, err.message, R.style.Customtoast).show()

                                if (binding.progressbar.isShown)
                                    binding.progressbar.visibility = View.GONE
                            }
                    }
                }
            }.addOnProgressListener {uploadTask ->
                val uploadingPercent = uploadTask.bytesTransferred * 100 / uploadTask.totalByteCount
                binding.progressbar.progress = uploadingPercent.toInt()
                if (!binding.progressbar.isShown)
                    binding.progressbar.visibility = View.VISIBLE

            }.addOnFailureListener {
                if (binding.progressbar.isShown)
                    binding.progressbar.visibility = View.GONE
            }
        }
    }
}