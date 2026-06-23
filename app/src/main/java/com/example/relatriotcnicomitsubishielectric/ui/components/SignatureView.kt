package com.example.relatriotcnicomitsubishielectric.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.relatriotcnicomitsubishielectric.ui.theme.LineGray
import com.example.relatriotcnicomitsubishielectric.ui.theme.MitsubishiRed
import java.io.ByteArrayOutputStream

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val strokes = mutableListOf<Pair<Path, Paint>>()
    private var current: Path? = null
    private val strokePaint = Paint().apply {
        color = 0xFF1B1F24.toInt()
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    private val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFBBC4CC.toInt()
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }
    private val placeholderLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFCDD4DA.toInt()
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    var isEmpty = true; private set
    var onChanged: ((Bitmap?) -> Unit)? = null

    private var restoredBitmap: Bitmap? = null

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0xFFFAFAF8.toInt())
        restoredBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        strokes.forEach { (p, paint) -> canvas.drawPath(p, paint) }
        current?.let { canvas.drawPath(it, strokePaint) }

        // Placeholder visível somente quando vazio
        if (isEmpty && restoredBitmap == null) {
            val cx = width / 2f
            val cy = height / 2f
            // Linha pontilhada de assinatura
            val lineY = cy + 18f
            val lineMargin = width * 0.12f
            canvas.drawLine(lineMargin, lineY, width - lineMargin, lineY, placeholderLinePaint)
            // Texto acima da linha
            canvas.drawText("Assine aqui", cx, lineY - 10f, placeholderPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                current = Path().also { it.moveTo(event.x, event.y) }
                isEmpty = false
            }
            MotionEvent.ACTION_MOVE -> current?.lineTo(event.x, event.y)
            MotionEvent.ACTION_UP -> {
                current?.let { strokes.add(it to Paint(strokePaint)) }
                current = null
                notifyChanged()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        strokes.clear(); current = null; restoredBitmap = null; isEmpty = true
        invalidate(); onChanged?.invoke(null)
    }

    fun getBitmap(): Bitmap? {
        if (width == 0 || height == 0) return null
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bmp))
        return if (isEmpty && restoredBitmap == null) null else bmp
    }

    fun restoreFromBase64(b64: String) {
        if (b64.isEmpty()) return
        try {
            val bytes = Base64.decode(b64, Base64.NO_WRAP)
            restoredBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            isEmpty = false; invalidate()
        } catch (_: Exception) {}
    }

    private fun notifyChanged() {
        getBitmap()?.let { bmp ->
            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            onChanged?.invoke(bmp)
        }
    }
}

@Composable
fun SignatureCanvas(
    savedBase64: String,
    onChanged: (Bitmap?) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewRef by remember { mutableStateOf<SignatureView?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { ctx ->
                SignatureView(ctx).also { view ->
                    view.onChanged = onChanged
                    viewRef = view
                }
            },
            update = { view ->
                if (savedBase64.isNotEmpty() && view.isEmpty) {
                    view.restoreFromBase64(savedBase64)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(1.dp, LineGray, RoundedCornerShape(4.dp))
        )
        TextButton(onClick = {
            viewRef?.clear()
            onClear()
        }) {
            Text("Limpar assinatura", color = MitsubishiRed)
        }
    }
}
