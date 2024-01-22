package com.brianm135.aidas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.ExecutorService
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.brianm135.aidas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        var selectedModel = 0

        val liveCameraBtn: View = findViewById(R.id.liveCameraBtn)
        val selectFromGalleryBtn: View = findViewById(R.id.selectFromGallaryBtn)
        val modelSelectionDropDown: Spinner = findViewById(R.id.modelSelectionDropDown)

        // Populate modelSelectionDropdown spinner
        ArrayAdapter.createFromResource(this, R.array.models,android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            modelSelectionDropDown.adapter = adapter
        }

        // Add click listener to LiveCameraBtn
        liveCameraBtn.setOnClickListener{
            // Get the selected model
            selectedModel = getSelectedModel(modelSelectionDropDown)

            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra("model", selectedModel)
            startActivity(intent)
        }

        // Add click listener to SelectFromGalleryBtn
        selectFromGalleryBtn.setOnClickListener{
            // Get the selected model
            selectedModel = getSelectedModel(modelSelectionDropDown)

            val intent = Intent(this, SelectFromGallery::class.java)
            intent.putExtra("model", selectedModel)
            startActivity(intent)
        }

    }

    private fun getSelectedModel(modelSelectionDropdown: Spinner): Int {
        return modelSelectionDropdown.selectedItemId.toInt()
    }


}

