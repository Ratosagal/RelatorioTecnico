package com.example.relatriotcnicomitsubishielectric

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.relatriotcnicomitsubishielectric.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class RatViewModel : ViewModel() {

    private val _state = MutableStateFlow(RatState())
    val state: StateFlow<RatState> = _state

    private var saveJob: Job? = null

    fun init(context: Context) {
        val raw = context.getSharedPreferences("rat_prefs", Context.MODE_PRIVATE)
            .getString("rat_draft", null) ?: return
        try { _state.value = ratStateFromJson(raw) } catch (_: Exception) {}
    }

    private fun save(context: Context) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(400)
            withContext(Dispatchers.IO) {
                context.getSharedPreferences("rat_prefs", Context.MODE_PRIVATE)
                    .edit().putString("rat_draft", _state.value.toJson()).apply()
            }
        }
    }

    fun clearAll(context: Context) {
        _state.value = RatState()
        context.getSharedPreferences("rat_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    // ── Seção 1 ──────────────────────────────────────────────────────────────

    fun setMultiDay(v: Boolean, ctx: Context) {
        _state.update { s ->
            val n = if (v) s.numDias else 1
            val days = s.dayTimes.toMutableList()
            while (days.size < n) days.add(DayTime())
            s.copy(isMultiDay = v, dayTimes = days.take(n))
        }
        save(ctx)
    }

    fun setNumDias(n: Int, ctx: Context) {
        val clamped = n.coerceIn(2, 30)
        _state.update { s ->
            val days = s.dayTimes.toMutableList()
            while (days.size < clamped) days.add(DayTime())
            s.copy(numDias = clamped, dayTimes = days.take(clamped))
        }
        save(ctx)
    }

    fun updateDay(idx: Int, day: DayTime, ctx: Context) {
        _state.update { s ->
            val days = s.dayTimes.toMutableList()
            if (idx < days.size) days[idx] = day
            s.copy(dayTimes = days)
        }
        save(ctx)
    }

    // ── Seção 2 ──────────────────────────────────────────────────────────────

    fun setNumOS(v: String, ctx: Context)            { _state.update { it.copy(numOS = v) }; save(ctx) }
    fun setDataAtendimento(v: String, ctx: Context)  { _state.update { it.copy(dataAtendimento = v) }; save(ctx) }
    fun setPrioridade(v: String, ctx: Context)       { _state.update { it.copy(prioridade = v) }; save(ctx) }
    fun setTecnico(v: String, ctx: Context)          { _state.update { it.copy(tecnico = v) }; save(ctx) }
    fun setTipoTarefa(v: String, ctx: Context)       { _state.update { it.copy(tipoTarefa = v) }; save(ctx) }

    // ── Seção 3 ──────────────────────────────────────────────────────────────

    fun setEmpresa(v: String, ctx: Context)          { _state.update { it.copy(empresa = v) }; save(ctx) }
    fun setEndereco(v: String, ctx: Context)         { _state.update { it.copy(endereco = v) }; save(ctx) }
    fun setContatoNome(v: String, ctx: Context)      { _state.update { it.copy(contatoNome = v) }; save(ctx) }
    fun setContatoEmail(v: String, ctx: Context)     { _state.update { it.copy(contatoEmail = v) }; save(ctx) }
    fun setContatoTelefone(v: String, ctx: Context)  { _state.update { it.copy(contatoTelefone = v) }; save(ctx) }

    // ── Seção 4 ──────────────────────────────────────────────────────────────

    fun setFabricanteMaquina(v: String, ctx: Context) { _state.update { it.copy(fabricanteMaquina = v) }; save(ctx) }
    fun setModeloMaquina(v: String, ctx: Context)     { _state.update { it.copy(modeloMaquina = v) }; save(ctx) }
    fun setSerieMaquina(v: String, ctx: Context)      { _state.update { it.copy(serieMaquina = v) }; save(ctx) }
    fun setModeloCNC(v: String, ctx: Context)         { _state.update { it.copy(modeloCNC = v) }; save(ctx) }
    fun setSerieCNC(v: String, ctx: Context)          { _state.update { it.copy(serieCNC = v) }; save(ctx) }
    fun setMaquinaParada(v: String, ctx: Context)     { _state.update { it.copy(maquinaParada = v) }; save(ctx) }
    fun setMaquinaGarantia(v: String, ctx: Context)   { _state.update { it.copy(maquinaGarantia = v) }; save(ctx) }

    // ── Seção 5 ──────────────────────────────────────────────────────────────

    fun setProblema(v: String, ctx: Context)  { _state.update { it.copy(problema = v) }; save(ctx) }
    fun setCausa(v: String, ctx: Context)     { _state.update { it.copy(causa = v) }; save(ctx) }
    fun setSolucao(v: String, ctx: Context)   { _state.update { it.copy(solucao = v) }; save(ctx) }

    // ── Seção 6 ──────────────────────────────────────────────────────────────

    fun setEquipInstalado(v: String, ctx: Context) { _state.update { it.copy(equipInstalado = v) }; save(ctx) }
    fun setDescInstalado(v: String, ctx: Context)  { _state.update { it.copy(descInstalado = v) }; save(ctx) }
    fun setEmprestimo(v: String, ctx: Context)     { _state.update { it.copy(emprestimo = v) }; save(ctx) }

    fun setEmpCNPJ(v: String, ctx: Context)           { _state.update { it.copy(empCNPJ = v) }; save(ctx) }
    fun setEmpCidade(v: String, ctx: Context)          { _state.update { it.copy(empCidade = v) }; save(ctx) }
    fun setEmpEstado(v: String, ctx: Context)          { _state.update { it.copy(empEstado = v) }; save(ctx) }
    fun setEmpCEP(v: String, ctx: Context)             { _state.update { it.copy(empCEP = v) }; save(ctx) }
    fun setEmpEndereco(v: String, ctx: Context)        { _state.update { it.copy(empEndereco = v) }; save(ctx) }
    fun setEmpRepNome(v: String, ctx: Context)         { _state.update { it.copy(empRepNome = v) }; save(ctx) }
    fun setEmpRepCidadania(v: String, ctx: Context)    { _state.update { it.copy(empRepCidadania = v) }; save(ctx) }
    fun setEmpRepEstCivil(v: String, ctx: Context)     { _state.update { it.copy(empRepEstCivil = v) }; save(ctx) }
    fun setEmpRepProfissao(v: String, ctx: Context)    { _state.update { it.copy(empRepProfissao = v) }; save(ctx) }
    fun setEmpRepCPF(v: String, ctx: Context)          { _state.update { it.copy(empRepCPF = v) }; save(ctx) }
    fun setEmpRepRG(v: String, ctx: Context)           { _state.update { it.copy(empRepRG = v) }; save(ctx) }
    fun setEmpDataContrato(v: String, ctx: Context)    { _state.update { it.copy(empDataContrato = v) }; save(ctx) }
    fun setEmpLocalContrato(v: String, ctx: Context)   { _state.update { it.copy(empLocalContrato = v) }; save(ctx) }
    fun setEmpClienteNome(v: String, ctx: Context)     { _state.update { it.copy(empClienteNome = v) }; save(ctx) }
    fun setEmpClienteCPF(v: String, ctx: Context)      { _state.update { it.copy(empClienteCPF = v) }; save(ctx) }
    fun setEmpTest1Nome(v: String, ctx: Context)       { _state.update { it.copy(empTest1Nome = v) }; save(ctx) }
    fun setEmpTest1CPF(v: String, ctx: Context)        { _state.update { it.copy(empTest1CPF = v) }; save(ctx) }
    fun setEmpTest2Nome(v: String, ctx: Context)       { _state.update { it.copy(empTest2Nome = v) }; save(ctx) }
    fun setEmpTest2CPF(v: String, ctx: Context)        { _state.update { it.copy(empTest2CPF = v) }; save(ctx) }

    fun updateEmpProduto(idx: Int, prod: EmpProduto, ctx: Context) {
        _state.update { s ->
            val list = s.empProdutos.toMutableList()
            if (idx < list.size) list[idx] = prod
            s.copy(empProdutos = list)
        }
        save(ctx)
    }

    fun addEmpProduto(ctx: Context) {
        _state.update { it.copy(empProdutos = it.empProdutos + EmpProduto()) }
        save(ctx)
    }

    fun removeEmpProduto(idx: Int, ctx: Context) {
        if (_state.value.empProdutos.size <= 1) return
        _state.update { it.copy(empProdutos = it.empProdutos.toMutableList().also { l -> l.removeAt(idx) }) }
        save(ctx)
    }

    // ── Seção 7 ──────────────────────────────────────────────────────────────

    fun setEquipRetirado(v: String, ctx: Context)    { _state.update { it.copy(equipRetirado = v) }; save(ctx) }
    fun setDescRetirado(v: String, ctx: Context)     { _state.update { it.copy(descRetirado = v) }; save(ctx) }
    fun setCondicaoRetirado(v: String, ctx: Context) { _state.update { it.copy(condicaoRetirado = v) }; save(ctx) }

    // ── Seção 9 ──────────────────────────────────────────────────────────────

    fun setLabInfo(v: String, ctx: Context) { _state.update { it.copy(labInfo = v) }; save(ctx) }

    // ── Seção 10 ─────────────────────────────────────────────────────────────

    fun setAssinaturaNome(v: String, ctx: Context)       { _state.update { it.copy(assinaturaNome = v) }; save(ctx) }
    fun setAssinaturaDocumento(v: String, ctx: Context)  { _state.update { it.copy(assinaturaDocumento = v) }; save(ctx) }

    fun saveSignature(bitmap: Bitmap?, ctx: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val b64 = if (bitmap == null || bitmap.width == 0) "" else {
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            }
            _state.update { it.copy(signatureBase64 = b64) }
            save(ctx)
        }
    }

    // ── Fotos ─────────────────────────────────────────────────────────────────

    fun addPhotos(context: Context, uris: List<Uri>, field: PhotoField) {
        viewModelScope.launch {
            val list = uris.mapNotNull { uri ->
                withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val raw = stream.readBytes()
                            val bmp = BitmapFactory.decodeByteArray(raw, 0, raw.size) ?: return@use null
                            val scaled = scaleBitmap(bmp, 1024)
                            val out = ByteArrayOutputStream()
                            scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
                            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                        }
                    } catch (_: Exception) { null }
                }
            }
            _state.update { s ->
                when (field) {
                    PhotoField.INSTALADO -> s.copy(fotosInstalado = s.fotosInstalado + list)
                    PhotoField.RETIRADO  -> s.copy(fotosRetirado  = s.fotosRetirado  + list)
                    PhotoField.RELATORIO -> s.copy(fotosRelatorio = s.fotosRelatorio + list)
                }
            }
            save(context)
        }
    }

    fun removePhoto(idx: Int, field: PhotoField, ctx: Context) {
        _state.update { s ->
            when (field) {
                PhotoField.INSTALADO -> s.copy(fotosInstalado = s.fotosInstalado.toMutableList().also { it.removeAt(idx) })
                PhotoField.RETIRADO  -> s.copy(fotosRetirado  = s.fotosRetirado.toMutableList().also  { it.removeAt(idx) })
                PhotoField.RELATORIO -> s.copy(fotosRelatorio = s.fotosRelatorio.toMutableList().also { it.removeAt(idx) })
            }
        }
        save(ctx)
    }

    private fun scaleBitmap(bmp: Bitmap, maxPx: Int): Bitmap {
        val w = bmp.width; val h = bmp.height
        if (w <= maxPx && h <= maxPx) return bmp
        val ratio = maxPx.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bmp, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}
