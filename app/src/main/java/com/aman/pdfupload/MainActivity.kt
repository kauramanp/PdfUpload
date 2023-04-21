package com.aman.pdfupload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import com.aman.pdfupload.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding 
    private  val TAG = "MainActivity"
    var storageRef = FirebaseStorage.getInstance()
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                Log.e(TAG, "permission granted")
                getImage.launch("application/pdf")
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.e(TAG, "permission declined")
                var alertDialog = AlertDialog.Builder(this)
                alertDialog.apply {
                    setTitle("Permission required")
                    setMessage("Permission required to run the app")
                    setCancelable(false)
                    setPositiveButton("Ok"){_,_-> requestPermission()}
                }
                alertDialog.show()

            }
        }


    val getImage =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            Log.e(TAG, "it uri ${it?.path}")
           // binding.ivImage.setImageURI(it)
            it?.let {
            storageRef.getReference(Calendar.getInstance().timeInMillis.toString()).putFile(it)
                .addOnSuccessListener {
                    System.out.println("Success $it")
                }.addOnFailureListener{
                    System.out.println("Error $it")
                }
        }
        }

    private fun requestPermission() {
        Log.e(TAG," request permission")
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        Log.e(TAG, "permission granted when")
                        getImage.launch("application/pdf")
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)->{
                        requestPermission()
                    }

                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }

        }
    }

}