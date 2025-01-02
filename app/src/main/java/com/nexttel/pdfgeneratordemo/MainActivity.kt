package com.nexttel.pdfgeneratordemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bitbyte.pdfgenerator.IPdfCreator
import com.bitbyte.pdfgenerator.PdfCreator
import com.nexttel.pdfgeneratordemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val pdfCreator: IPdfCreator = PdfCreator(this)
    private var permissionCallBack: ((onGranted: Boolean) -> Unit)? = null
    private lateinit var binding: ActivityMainBinding

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionCallBack?.invoke(isGranted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.button.setOnClickListener {
            requestStoragePermission{
                if (it){
                    pdfCreator.createPdfFromViews(
                        views = listOf(binding.imageView),
                        fileName = "test.pdf",
                        showPdf = true,
                        onPdfCreated = { file, message ->
                            if (file != null) {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }else{
                    Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun requestStoragePermission(callBack: (onGranted: Boolean) -> Unit = {}) {
        permissionCallBack = callBack
        when {
            // For Android 11+ (Scoped Storage)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> callBack.invoke(true)

            // For Android 6-10: Check and request permission
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED -> {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            else -> callBack.invoke(true)
        }
    }
}