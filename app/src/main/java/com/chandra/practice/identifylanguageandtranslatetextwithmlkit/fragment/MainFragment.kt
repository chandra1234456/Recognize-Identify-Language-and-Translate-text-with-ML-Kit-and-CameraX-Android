package com.chandra.practice.identifylanguageandtranslatetextwithmlkit.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.databinding.FragmentMainBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainFragment : Fragment() {
    private lateinit var mainBinding : FragmentMainBinding

    companion object {
        private val REQUESTCAMERA = 100
        private lateinit var photoUri: Uri
    }

    override fun onCreateView(
        inflater : LayoutInflater , container : ViewGroup? ,
        savedInstanceState : Bundle? ,
                             ) : View {
        mainBinding = FragmentMainBinding.inflate(layoutInflater)
        mainBinding.btnCaptureImage.setOnClickListener {
            if (isCameraPermissionGranted()) {
            dispatchTakePictureIntent()
            }else{
                requestCameraPermission()
            }
        }
        return mainBinding.root
    }


    // Method to initiate camera
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                val photoFile = createImageFile()
                //photoUri = Uri.fromFile(photoFile)
                 photoUri = FileProvider.getUriForFile(
                        requireContext() ,
                        "com.chandra.practice.identifylanguageandtranslatetextwithmlkit.provider" ,  //(use your app signature + ".provider" )
                        photoFile
                                                         )

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, REQUESTCAMERA)
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error while accessing camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No camera available", Toast.LENGTH_SHORT).show()
        }
    }

    // Create a file to save the image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timestamp}_", /* prefix */
                ".jpg",                /* suffix */
                storageDir             /* directory */
                                  )
    }

    // Method to handle the result of the camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUESTCAMERA && resultCode == RESULT_OK) {
            // Load the image from the URI
           // val imageBitmap = BitmapFactory.decodeFile(photoUri.path)
            val imageBitmap = try {
                val inputStream = requireContext().contentResolver.openInputStream(photoUri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            }
            // Check if the image is not null and then set it to ImageView
            if (imageBitmap != null) {
                // You can scale the bitmap if it's too large for display
                val scaledBitmap = scaleBitmap(imageBitmap, 1024, 1024)
                // Set the scaled bitmap to the ImageView
                mainBinding.shapeableImageView.setImageBitmap(scaledBitmap)
                toastMessage("Image Captured Successfully")
            } else {
                toastMessage("Failed to Capture Image")
            }
        } else {
            Toast.makeText(requireContext(), "Camera action was cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to scale bitmap to a desired size (for memory and performance reasons)
    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, false)
    }

    private fun toastMessage(message : String) {
        Toast.makeText(requireContext() , message , Toast.LENGTH_SHORT).show()

    }
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
                                                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (!isCameraPermissionGranted()) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUESTCAMERA
                                             )
        } else {
            dispatchTakePictureIntent()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode : Int ,
        permissions : Array<out String> ,
        grantResults : IntArray ,
                                           ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUESTCAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                dispatchTakePictureIntent()
            } else {
                // Permission denied, show a message
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

}