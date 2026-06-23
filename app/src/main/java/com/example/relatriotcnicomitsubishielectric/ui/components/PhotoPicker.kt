package com.example.relatriotcnicomitsubishielectric.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.relatriotcnicomitsubishielectric.RatViewModel
import com.example.relatriotcnicomitsubishielectric.data.PhotoField
import com.example.relatriotcnicomitsubishielectric.ui.theme.*

@Composable
fun PhotoSection(
    vm: RatViewModel,
    photos: List<String>,
    field: PhotoField,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) vm.addPhotos(context, uris, field)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Steel),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📷  Adicionar fotos",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (photos.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 2.dp)
                ) { Text(photos.size.toString(), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
            }
        }

        if (photos.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            val rows = photos.chunked(3)
            rows.forEachIndexed { rowIdx, row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEachIndexed { colIdx, b64 ->
                        val idx = rowIdx * 3 + colIdx
                        PhotoThumb(
                            base64 = b64,
                            onRemove = { vm.removePhoto(idx, field, context) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumb(
    base64: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(base64) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (_: Exception) { null }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, LineGray, RoundedCornerShape(4.dp))
            .background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp))
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(26.dp)
                .padding(2.dp)
                .background(Color.Black.copy(.6f), CircleShape)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
