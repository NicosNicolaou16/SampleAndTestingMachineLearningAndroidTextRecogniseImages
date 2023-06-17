package com.nicos.sampleandtestingmachinelearningandroidtextrecogniseimages

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nicos.sampleandtestingmachinelearningandroidtextrecogniseimages.ui.theme.SampleAndTestingMachineLearningAndroidTextRecogniseImagesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleAndTestingMachineLearningAndroidTextRecogniseImagesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainProcess(context = this)
                }
            }
        }
    }

    @Composable
    fun MainProcess(context: Context, modifier: Modifier = Modifier) {
        val uriValue = remember { mutableStateOf(Uri.EMPTY) }
        val openDialog = remember { mutableStateOf(false) }
        val displayValue = remember { mutableStateOf("") }
        if (openDialog.value) AlertDialog(displayValue.value, openDialog)
        val galleryLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    uriValue.value = uri
                }
            }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(start = 15.dp, end = 15.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                ElevatedButton(
                    content = {
                        Text(
                            text = stringResource(id = R.string.galleryImage),
                            style = TextStyle(fontSize = 21.sp)
                        )
                    },
                    modifier = modifier.size(height = 70.dp, width = 250.dp),
                    onClick = {
                        galleryLauncher.launch(
                            "image/*"
                        )
                    }
                )
            }
            Spacer(Modifier.width(15.dp))
            Box(contentAlignment = Alignment.Center) {
                ElevatedButton(
                    content = {
                        Text(
                            text = stringResource(id = R.string.scan),
                            style = TextStyle(fontSize = 21.sp)
                        )
                    },
                    modifier = modifier.size(height = 70.dp, width = 250.dp),
                    onClick = {
                        if (uriValue.value != Uri.EMPTY) {
                            handleTextRecognition(
                                context = context,
                                uriValue = uriValue
                            ) { result ->
                                displayValue.value = result
                                openDialog.value = true
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handleTextRecognition(
        context: Context,
        uriValue: MutableState<Uri>,
        result: (String) -> Unit
    ) {
        val bitmap = convertUriToBitmap(
            contentResolver = context.contentResolver,
            uriValue.value
        )
        val inputImage = InputImage.fromBitmap(bitmap!!, 0)
        val recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                result(visionText.text)
            }
            .addOnFailureListener { e ->
                result(e.message ?: getString(R.string.galleryImage))
            }
    }

    @Composable
    private fun AlertDialog(
        displayValue: String,
        openDialog: MutableState<Boolean>,
    ) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = stringResource(id = R.string.galleryImage))
            },
            text = {
                Text(
                    displayValue
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog.value = false
                    }
                ) {
                    Text(stringResource(id = R.string.ok))
                }
            },
        )
    }

    private fun convertUriToBitmap(
        contentResolver: ContentResolver,
        uri: Uri?
    ): Bitmap? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source: ImageDecoder.Source? =
                uri?.let { ImageDecoder.createSource(contentResolver, it) }
            source?.let { ImageDecoder.decodeBitmap(it) }
        }
    }

}