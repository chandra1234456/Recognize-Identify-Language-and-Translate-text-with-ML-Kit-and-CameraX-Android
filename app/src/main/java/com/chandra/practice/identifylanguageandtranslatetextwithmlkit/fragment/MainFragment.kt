package com.chandra.practice.identifylanguageandtranslatetextwithmlkit.fragment

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.R
import com.chandra.practice.identifylanguageandtranslatetextwithmlkit.databinding.FragmentMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainFragment : Fragment() {
    private lateinit var mainBinding : FragmentMainBinding
    private lateinit var textRecognition : TextRecognizer

    companion object {
        private val REQUESTCAMERA = 100
        private lateinit var photoUri : Uri
        private val CROP_IMAGE_REQUEST = 2
        private lateinit var croppedImageUri : Uri
    }

    override fun onCreateView(
        inflater : LayoutInflater , container : ViewGroup? ,
        savedInstanceState : Bundle? ,
                             ) : View {
        mainBinding = FragmentMainBinding.inflate(layoutInflater)
        textRecognition = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        mainBinding.btnCaptureImage.setOnClickListener {
            if (isCameraPermissionGranted()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }
        mainBinding.ivCopyRecognizedText.setOnClickListener {
            val copyText = mainBinding.tvExtractedText.text.toString()
            if (copyText.isNotEmpty()) {
                copyTheRecognizedText(copyText)
            } else {
                toastMessage("Text Should Not Empty")
            }
        }
        mainBinding.btnRecognitionImage.setOnClickListener {
            recognitionImageFromText()
        }
        mainBinding.btnCropImage.setOnClickListener {
            showMaterialDialog()
        }
        return mainBinding.root
    }

    // Function to show the Material AlertDialog with custom layout
    private fun showMaterialDialog() {
        val dialogView = layoutInflater.inflate(R.layout.crop_image, null)

        val imageView: ImageView = dialogView.findViewById(R.id.cropImageView)
        val progressBar: ProgressBar = dialogView.findViewById(R.id.progressBar)

        // Use Picasso or Glide to load the image from URI into ImageView
        Picasso.get()
                .load(photoUri)
                .into(imageView)

        // Create the dialog with the custom layout
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Crop Image")
                .setIcon(R.drawable.ic_launcher_background) // Optionally set an icon
                .setView(dialogView) // Set the custom layout
                .setPositiveButton("OK") { dialog, which ->
                    // Handle OK button click (e.g., crop the image)
                    cropImage(photoUri)
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    // Handle Cancel button click
                    dialog.dismiss()
                }
                .show() // Display the dialog
    }

    // Method to copy text to clipboard
    private fun copyTheRecognizedText(text : String) {
        val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label" , text)
        clipboard.setPrimaryClip(clip)
        // Show a Toast message to confirm
        Toast.makeText(requireContext() , "Text copied to clipboard" , Toast.LENGTH_SHORT).show()
    }

    private fun recognitionImageFromText() {
        if (photoUri != null) {
            val inputImage = InputImage.fromFilePath(requireContext() , photoUri)
            val textResult =
                textRecognition.process(inputImage).addOnSuccessListener { recognizedText ->
                    Log.d("TAG" , "addOnSuccessListener: ${recognizedText.text}")
                    mainBinding.tvExtractedText.text = recognizedText.text
                }.addOnFailureListener { error ->
                    Log.d("TAG" , "addOnFailureListener: ${error.message}")
                    toastMessage("${error.message}")
                }
        } else {
            toastMessage("Please Capture Image")
        }
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
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT , photoUri)
                startActivityForResult(takePictureIntent , REQUESTCAMERA)
            } catch (e : IOException) {
                e.printStackTrace()
                Toast.makeText(
                        requireContext() ,
                        "Error while accessing camera" ,
                        Toast.LENGTH_SHORT
                              ).show()
            }
        } else {
            Toast.makeText(requireContext() , "No camera available" , Toast.LENGTH_SHORT).show()
        }
    }

    // Create a file to save the image
    @Throws(IOException::class)
    private fun createImageFile() : File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss" , Locale.US).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timestamp}_" , /* prefix */
                ".jpg" ,                /* suffix */
                storageDir             /* directory */
                                  )
    }

    // Method to handle the result of the camera
    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if (requestCode == REQUESTCAMERA && resultCode == RESULT_OK) {
            // Load the image from the URI
            // val imageBitmap = BitmapFactory.decodeFile(photoUri.path)
            val imageBitmap = try {
                val inputStream = requireContext().contentResolver.openInputStream(photoUri)
                BitmapFactory.decodeStream(inputStream)
            } catch (e : FileNotFoundException) {
                e.printStackTrace()
                null
            }
            // Check if the image is not null and then set it to ImageView
            if (imageBitmap != null) {
                // You can scale the bitmap if it's too large for display
                val scaledBitmap = scaleBitmap(imageBitmap , 1024 , 1024)
                // Set the scaled bitmap to the ImageView
                mainBinding.shapeableImageView.setImageBitmap(scaledBitmap)
                toastMessage("Image Captured Successfully")
            } else {
                toastMessage("Failed to Capture Image")
            }
        } else {
            Toast.makeText(
                    requireContext() ,
                    "Camera action was cancelled or failed" ,
                    Toast.LENGTH_SHORT
                          ).show()
        }
    }

    // Function to scale bitmap to a desired size (for memory and performance reasons)
    private fun scaleBitmap(bitmap : Bitmap , maxWidth : Int , maxHeight : Int) : Bitmap {
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

        return Bitmap.createScaledBitmap(bitmap , finalWidth , finalHeight , false)
    }

    private fun toastMessage(message : String) {
        Toast.makeText(requireContext() , message , Toast.LENGTH_SHORT).show()

    }

    private fun isCameraPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext() ,
                android.Manifest.permission.CAMERA
                                                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (! isCameraPermissionGranted()) {
            ActivityCompat.requestPermissions(
                    requireActivity() ,
                    arrayOf(android.Manifest.permission.CAMERA) ,
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
        super.onRequestPermissionsResult(requestCode , permissions , grantResults)

        if (requestCode == REQUESTCAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                dispatchTakePictureIntent()
            } else {
                // Permission denied, show a message
                Toast.makeText(
                        requireContext() ,
                        "Camera permission is required" ,
                        Toast.LENGTH_SHORT
                              ).show()
            }
        }
    }

    // Method to crop the selected image
    private fun cropImage(uri : Uri) {
        val intent = Intent("com.android.activity.CROP")
        intent.setDataAndType(uri , "image/*")

        // Enable crop functionality
        intent.putExtra("crop" , "true")
        intent.putExtra("aspectX" , 1)  // Aspect ratio (optional)
        intent.putExtra("aspectY" , 1)  // Aspect ratio (optional)
        intent.putExtra("outputX" , 500)  // Output width
        intent.putExtra("outputY" , 500)  // Output height
        intent.putExtra("return-data" , true)

        try {
            startActivityForResult(intent , CROP_IMAGE_REQUEST)
        } catch (e : ActivityNotFoundException) {
            Toast.makeText(requireContext() , "Crop feature not available" , Toast.LENGTH_SHORT)
                    .show()
        }
    }


}