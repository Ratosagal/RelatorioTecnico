package com.example.relatriotcnicomitsubishielectric.report

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.relatriotcnicomitsubishielectric.data.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private const val W = 595   // A4 largura (pontos)
private const val H = 842   // A4 altura (pontos)
private const val ML = 40f  // margem esquerda
private const val MR = 40f  // margem direita
private const val MT = 40f  // margem topo
private const val MB = 40f  // margem base
private val CW = W - ML - MR // largura do conteúdo

// ─── Cores ───────────────────────────────────────────────────────────────────

private val RED    = Color.parseColor("#C8102E")
private val STEEL  = Color.parseColor("#2E3A46")
private val LGRAY  = Color.parseColor("#DCD7CC")
private val MGRAY  = Color.parseColor("#F2F0EB")
private val DTEXT  = Color.parseColor("#1B1F24")
private val STEXT  = Color.parseColor("#5B6B7A")
private val WHITE  = Color.WHITE

// ─── Pintura helpers ─────────────────────────────────────────────────────────

private fun textPaint(color: Int, size: Float, bold: Boolean = false, mono: Boolean = false) =
    Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = color
        it.textSize = size
        it.typeface = when {
            mono -> Typeface.MONOSPACE
            bold -> Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT
        }
    }

private fun rectPaint(color: Int, style: Paint.Style = Paint.Style.FILL) =
    Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = color; it.style = style }

// ─── Construtor de páginas ────────────────────────────────────────────────────

private class Pages(private val doc: PdfDocument, val logoBitmap: Bitmap? = null) {
    private var page: PdfDocument.Page? = null
    var canvas: Canvas = Canvas()
        private set
    var y = MT
    private var num = 1

    fun start() {
        val info = PdfDocument.PageInfo.Builder(W, H, num++).create()
        page = doc.startPage(info)
        canvas = page!!.canvas
        y = MT
        drawPageBg()
    }

    private fun drawPageBg() { canvas.drawColor(WHITE) }

    fun finish() { page?.let { doc.finishPage(it) }; page = null }

    fun need(space: Float) {
        if (y + space > H - MB) { finish(); start() }
    }
}

// ─── Lógica de desenho ───────────────────────────────────────────────────────

private fun Pages.drawLogo(x: Float, yy: Float, size: Float = 36f) {
    val cx = x + size / 2f; val cy = yy + size / 2f
    val paint = rectPaint(RED)
    repeat(3) { i ->
        canvas.save()
        canvas.rotate(i * 120f, cx, cy)
        val path = Path().apply {
            moveTo(cx, cy)
            lineTo(cx - size * 0.17f, cy - size * 0.17f)
            lineTo(cx, cy - size * 0.40f)
            lineTo(cx + size * 0.17f, cy - size * 0.17f)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.restore()
    }
}

private fun Pages.drawHeader() {
    // Fundo steel
    canvas.drawRect(0f, 0f, W.toFloat(), 70f, rectPaint(STEEL))

    // Logo PNG (se disponível) ou fallback geométrico
    val logoSize = 44f
    val logoLeft = 14f
    val logoTop  = (70f - logoSize) / 2f
    if (logoBitmap != null) {
        val dst = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
        canvas.drawBitmap(logoBitmap, null, dst,
            Paint(Paint.ANTI_ALIAS_FLAG).also { it.isFilterBitmap = true })
    } else {
        drawLogo(logoLeft, logoTop, logoSize)
    }

    // Texto da marca
    val brandPaint = textPaint(WHITE, 12f, bold = true).also { it.letterSpacing = -0.02f }
    canvas.drawText("MITSUBISHI", 68f, 32f, brandPaint)
    canvas.drawText("ELECTRIC",   68f, 48f, brandPaint)

    // Separador vertical
    canvas.drawRect(155f, 15f, 156f, 55f, rectPaint(WHITE).also { it.alpha = 60 })

    // Título
    canvas.drawText("Relatório de Assistência Técnica", 165f, 35f, textPaint(WHITE, 14f, bold = true))
    canvas.drawText("CNC / Automação Industrial", 165f, 52f, textPaint(Color.parseColor("#C9D2DA"), 10f))
    y = 85f
}

private fun Pages.sectionTitle(num: Int, title: String) {
    need(30f)
    // badge vermelho
    val badgeRect = RectF(ML, y, ML + 22f, y + 22f)
    canvas.drawRoundRect(badgeRect, 3f, 3f, rectPaint(RED))
    canvas.drawText(num.toString(), ML + 6f, y + 16f, textPaint(WHITE, 12f, bold = true))
    // título
    canvas.drawText(title.uppercase(), ML + 28f, y + 16f,
        textPaint(STEEL, 12f, bold = true).also { it.letterSpacing = 0.04f })
    y += 26f
    canvas.drawLine(ML, y, ML + CW, y, rectPaint(LGRAY).also { it.strokeWidth = 1f; it.style = Paint.Style.STROKE })
    y += 8f
}

private fun Pages.row(label: String, value: String) {
    need(22f)
    // Fundo label
    canvas.drawRect(ML, y, ML + CW * 0.34f, y + 18f, rectPaint(MGRAY))
    canvas.drawRect(ML, y, ML + CW, y + 18f, rectPaint(LGRAY).also { it.style = Paint.Style.STROKE; it.strokeWidth = .5f })
    canvas.drawText(label, ML + 4f, y + 13f, textPaint(DTEXT, 9f, bold = true))
    val valX = ML + CW * 0.34f + 4f
    canvas.drawText(value.ifEmpty { "—" }.take(80), valX, y + 13f, textPaint(DTEXT, 9.5f))
    y += 19f
}

private fun Pages.multiLineRow(label: String, value: String) {
    if (value.isEmpty()) { row(label, "—"); return }
    val lines = value.split("\n")
    val lineH = 16f
    val totalH = 20f + lines.size * lineH
    need(totalH)
    // label
    canvas.drawText(label.uppercase(), ML, y + 12f, textPaint(STEXT, 9f, bold = true))
    y += 16f
    // border
    val rect = RectF(ML, y, ML + CW, y + lines.size * lineH + 6f)
    canvas.drawRect(rect, rectPaint(LGRAY).also { it.style = Paint.Style.STROKE; it.strokeWidth = .5f })
    lines.forEach { line ->
        canvas.drawText(line.take(90), ML + 4f, y + 14f, textPaint(DTEXT, 9.5f))
        y += lineH
    }
    y += 8f
}

private fun Pages.drawPhotos(photos: List<String>) {
    if (photos.isEmpty()) return
    val thumbSize = 130f; val gap = 10f
    val perRow = 3
    photos.chunked(perRow).forEach { chunk ->
        need(thumbSize + gap + 6f)
        chunk.forEachIndexed { i, b64 ->
            val x = ML + i * (thumbSize + gap)
            try {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@forEachIndexed
                val dst = RectF(x, y, x + thumbSize, y + thumbSize)
                canvas.drawBitmap(bmp, null, dst, null)
            } catch (_: Exception) {}
            // borda
            canvas.drawRect(
                RectF(x, y, x + thumbSize, y + thumbSize),
                rectPaint(LGRAY).also { it.style = Paint.Style.STROKE; it.strokeWidth = .5f }
            )
        }
        y += thumbSize + gap
    }
    y += 6f
}

private fun Pages.drawSignature(b64: String, name: String = "", doc: String = "") {
    if (b64.isEmpty()) {
        need(30f)
        canvas.drawText("Assinatura não coletada.", ML, y + 12f, textPaint(STEXT, 9.5f))
        if (name.isNotEmpty()) {
            y += 16f
            canvas.drawText("Nome: $name", ML, y + 12f, textPaint(DTEXT, 9f))
        }
        y += 24f
        return
    }
    need(110f)
    try {
        val bytes = Base64.decode(b64, Base64.DEFAULT)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return
        val aspect = bmp.width.toFloat() / bmp.height.toFloat()
        val sigW = (CW * 0.45f).coerceAtMost(220f)
        val sigH = (sigW / aspect).coerceAtMost(80f)
        
        // Desenha o bitmap da assinatura
        val dst = RectF(ML, y, ML + sigW, y + sigH)
        canvas.drawBitmap(bmp, null, dst, null)
        y += sigH + 2f
        
        // Linha de assinatura
        canvas.drawLine(ML, y, ML + sigW + 40f, y, rectPaint(DTEXT).also { it.strokeWidth = .6f })
        y += 12f
        
        // Identificação abaixo da linha
        if (name.isNotEmpty()) {
            canvas.drawText(name.uppercase(), ML, y, textPaint(DTEXT, 9f, bold = true))
            y += 11f
        }
        if (doc.isNotEmpty()) {
            canvas.drawText("Documento: $doc", ML, y, textPaint(DTEXT, 8f))
            y += 11f
        }
        y += 10f
    } catch (_: Exception) { }
}

private fun Pages.spacer(h: Float = 12f) { y += h }

private fun dateBR(iso: String): String {
    if (iso.isEmpty()) return "—"
    val p = iso.split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
}

// ─── Geração do Relatório ─────────────────────────────────────────────────────

fun generateRelatorio(context: Context, s: RatState): File {
    val logoBmp = try {
        BitmapFactory.decodeResource(context.resources,
            context.resources.getIdentifier("ic_mitsubishi_logo", "drawable", context.packageName))
    } catch (_: Exception) { null }

    val doc = PdfDocument()
    val p = Pages(doc, logoBmp)
    p.start()
    p.drawHeader()
    p.spacer(8f)

    // 1 – Ponto
    p.sectionTitle(1, "Marcação de Ponto")
    val (grandPausa, grandTotal) = calcGrandTotal(s.dayTimes)
    s.dayTimes.forEachIndexed { idx, day ->
        if (s.isMultiDay) {
            p.need(16f)
            p.canvas.drawText("DIA ${idx + 1}".uppercase(), ML, p.y + 12f,
                textPaint(WHITE, 10f, bold = true).also { /* bkg */ })
            // fundo azul aço para label dia
            val lw = 60f
            p.canvas.drawRoundRect(RectF(ML, p.y, ML + lw, p.y + 14f), 3f, 3f, rectPaint(STEEL))
            p.canvas.drawText("DIA ${idx + 1}", ML + 4f, p.y + 11f, textPaint(WHITE, 9f, bold = true))
            p.y += 18f
        }
        val (dp, dt) = calcDay(day)
        p.row("Check-in",              dateBR(day.ciData) + (if (day.ciHora.isEmpty()) "" else " às ${day.ciHora}"))
        p.row("Início da pausa",       dateBR(day.ipData) + (if (day.ipHora.isEmpty()) "" else " às ${day.ipHora}"))
        p.row("Fim da pausa",          dateBR(day.fpData) + (if (day.fpHora.isEmpty()) "" else " às ${day.fpHora}"))
        p.row("Check-out",             dateBR(day.coData) + (if (day.coHora.isEmpty()) "" else " às ${day.coHora}"))
        p.row("Motivo da pausa",       day.motivo)
        p.row("Justificativa",         day.just.ifEmpty { "—" })
        p.row("Tempo em pausa",        dp)
        p.row("Tempo no atendimento",  dt)
        p.spacer(8f)
    }
    if (s.dayTimes.size > 1) {
        p.row("TOTAL EM PAUSA",        grandPausa)
        p.row("TOTAL NO ATENDIMENTO",  grandTotal)
    }
    p.spacer()

    // 2 – Atendimento
    p.sectionTitle(2, "Dados do Atendimento")
    p.row("Nº da tarefa / OS",      s.numOS)
    p.row("Data do atendimento",    dateBR(s.dataAtendimento))
    p.row("Prioridade",             s.prioridade)
    p.row("Técnico responsável",    s.tecnico)
    p.row("Tipo de tarefa",         s.tipoTarefa)
    p.spacer()

    // 3 – Cliente
    p.sectionTitle(3, "Dados do Cliente")
    p.row("Empresa",        s.empresa)
    p.row("Endereço",       s.endereco)
    p.row("Nome do contato", s.contatoNome)
    p.row("E-mail",         s.contatoEmail)
    p.row("Telefone",       s.contatoTelefone)
    p.spacer()

    // 4 – Máquina
    p.sectionTitle(4, "Dados da Máquina")
    p.row("Fabricante",       s.fabricanteMaquina)
    p.row("Modelo",           s.modeloMaquina)
    p.row("Nº série (máq.)", s.serieMaquina)
    p.row("Modelo CNC",       s.modeloCNC)
    p.row("Nº série (CNC)",  s.serieCNC)
    p.row("Máquina parada?", s.maquinaParada)
    p.row("Em garantia?",    s.maquinaGarantia)
    p.spacer()

    // 5 – Diagnóstico
    p.sectionTitle(5, "Diagnóstico")
    p.multiLineRow("Defeito constatado / Problema relatado", s.problema)
    p.spacer(4f)
    p.multiLineRow("Causa", s.causa)
    p.spacer(4f)
    p.multiLineRow("Solução aplicada", s.solucao)
    p.spacer()

    // 6 – Instalados
    p.sectionTitle(6, "Equipamentos Instalados")
    p.row("Foi instalado equipamento?", s.equipInstalado)
    if (s.equipInstalado == "Sim") {
        p.multiLineRow("Descrição", s.descInstalado)
        if (s.fotosInstalado.isNotEmpty()) {
            p.need(16f)
            p.canvas.drawText("Fotos do equipamento instalado:", ML, p.y + 11f, textPaint(STEXT, 9f, bold = true))
            p.y += 16f
            p.drawPhotos(s.fotosInstalado)
        }
    }
    p.spacer()

    // 7 – Retirados
    p.sectionTitle(7, "Equipamentos Retirados")
    p.row("Foi retirado equipamento?", s.equipRetirado)
    if (s.equipRetirado == "Sim") {
        p.multiLineRow("Descrição", s.descRetirado)
        p.row("Condição", s.condicaoRetirado)
        if (s.fotosRetirado.isNotEmpty()) {
            p.need(16f)
            p.canvas.drawText("Fotos do equipamento retirado:", ML, p.y + 11f, textPaint(STEXT, 9f, bold = true))
            p.y += 16f
            p.drawPhotos(s.fotosRetirado)
        }
    }
    p.spacer()

    // 8 – Fotos gerais
    p.sectionTitle(8, "Fotos Gerais do Relatório")
    if (s.fotosRelatorio.isEmpty()) {
        p.need(16f)
        p.canvas.drawText("Nenhuma foto anexada.", ML, p.y + 11f, textPaint(STEXT, 9.5f))
        p.y += 18f
    } else {
        p.drawPhotos(s.fotosRelatorio)
    }
    p.spacer()

    // 9 – Lab
    p.sectionTitle(9, "Informações para o Laboratório")
    p.multiLineRow("", s.labInfo.ifEmpty { "—" })
    p.spacer()

    // 10 – Assinatura
    p.sectionTitle(10, "Assinatura do Cliente")
    p.spacer(6f)
    p.drawSignature(s.signatureBase64, s.assinaturaNome, s.assinaturaDocumento)
    p.spacer(8f)

    // Rodapé
    p.need(20f)
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(java.util.Date())
    p.canvas.drawText(
        "Mitsubishi Electric do Brasil — RAT — Gerado em $today",
        ML, p.y + 11f, textPaint(STEXT, 8f)
    )

    p.finish()

    val safe = (s.empresa.ifEmpty { "cliente" }).replace(Regex("[^a-zA-Z0-9]"), "_")
    val name = "RAT_${safe}_${s.dataAtendimento.replace("-", "")}.pdf"
    val file = File(context.cacheDir, name)
    file.outputStream().use { doc.writeTo(it) }
    doc.close()
    return file
}

// ─── Geração do Contrato ──────────────────────────────────────────────────────

fun generateContrato(context: Context, s: RatState): File {
    val logoBmp = try {
        BitmapFactory.decodeResource(context.resources,
            context.resources.getIdentifier("ic_mitsubishi_logo", "drawable", context.packageName))
    } catch (_: Exception) { null }

    val doc = PdfDocument()
    val p = Pages(doc, logoBmp)
    p.start()
    p.drawHeader()
    p.spacer(10f)

    val meses = listOf("janeiro","fevereiro","março","abril","maio","junho",
        "julho","agosto","setembro","outubro","novembro","dezembro")
    val dataExtenso = if (s.empDataContrato.isEmpty()) "_____ de __________________ de 20____"
    else {
        val pts = s.empDataContrato.split("-")
        if (pts.size == 3) "${pts[2].trimStart('0')} de ${meses.getOrNull(pts[1].toIntOrNull()?.minus(1) ?: 0) ?: ""} de ${pts[0]}"
        else s.empDataContrato
    }

    fun Pages.centerText(text: String, yy: Float, size: Float, bold: Boolean = false) {
        val paint = textPaint(DTEXT, size, bold)
        val tw = paint.measureText(text)
        canvas.drawText(text, (W - tw) / 2f, yy, paint)
    }

    p.need(50f)
    p.centerText("TERMO DE COMPROMISSO, AUTORIZAÇÃO E", p.y + 14f, 12f, true)
    p.y += 18f
    p.centerText("RESPONSABILIDADE POR PRODUTO DE DEMONSTRAÇÃO", p.y + 14f, 12f, true)
    p.spacer(20f)

    fun Pages.para(text: String, indent: Boolean = false) {
        val x = if (indent) ML + 30f else ML
        val paint = textPaint(DTEXT, 9.5f)
        val maxW = CW - if (indent) 30f else 0f
        val words = text.split(" ")
        var line = ""
        words.forEach { word ->
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) > maxW) {
                need(14f); canvas.drawText(line, x, y + 10f, paint); y += 13f; line = word
            } else line = test
        }
        if (line.isNotEmpty()) { need(14f); canvas.drawText(line, x, y + 10f, paint); y += 13f }
        spacer(6f)
    }

    fun Pages.clausula(text: String) {
        need(20f)
        canvas.drawText(text.uppercase(), ML, y + 12f, textPaint(DTEXT, 10f, bold = true))
        y += 18f
    }

    p.para("Pelo presente Instrumento Particular, e na melhor forma de direito, as partes adiante nominadas e qualificadas, de um lado:")
    p.para(
        "MITSUBISHI ELECTRIC DO BRASIL COMÉRCIO E SERVIÇOS LTDA., CNPJ/MF nº 46.550.505/0008-05, " +
        "Av. Gisele Constantino, nº 1578, Votorantim/SP, CEP 18.110-650, doravante \"MEB\"; e,",
        indent = true
    )
    p.para(
        "${s.empresa.ifEmpty { "_________________________" }}, CNPJ/MF nº ${s.empCNPJ.ifEmpty { "___________________" }}, " +
        "${s.empEndereco.ifEmpty { s.endereco.ifEmpty { "__________________________" } }}, " +
        "${s.empCidade.ifEmpty { "______________" }}/${s.empEstado.ifEmpty { "__" }}, CEP ${s.empCEP.ifEmpty { "_________" }}, " +
        "representada por ${s.empRepNome.ifEmpty { "______________________________" }}, " +
        "CPF ${s.empRepCPF.ifEmpty { "________________" }}, doravante \"CLIENTE\";",
        indent = true
    )
    p.spacer(4f)

    // Tabela de produtos
    p.need(30f)
    p.canvas.drawText("Tabela A — Produto(s) em empréstimo:", ML, p.y + 12f, textPaint(DTEXT, 10f, bold = true))
    p.y += 18f
    val cols = listOf("Código", "Descrição", "Série", "Qtde", "NF", "Modelo Máq.", "Série Máq.", "Data")
    val colW = CW / cols.size
    p.need(18f)
    cols.forEachIndexed { i, c ->
        val cx = ML + i * colW
        p.canvas.drawRect(cx, p.y, cx + colW, p.y + 16f, rectPaint(STEEL))
        p.canvas.drawText(c, cx + 3f, p.y + 12f, textPaint(WHITE, 7f, bold = true))
    }
    p.y += 16f
    s.empProdutos.forEach { prod ->
        val vals = listOf(prod.cod, prod.desc, prod.serie, prod.qtde, prod.nf, prod.modeloMaq, prod.serieMaq, dateBR(prod.data))
        val rowH = 16f
        p.need(rowH)
        vals.forEachIndexed { i, v ->
            val cx = ML + i * colW
            p.canvas.drawRect(
                RectF(cx, p.y, cx + colW, p.y + rowH),
                rectPaint(LGRAY).also { it.style = Paint.Style.STROKE; it.strokeWidth = .5f }
            )
            p.canvas.drawText(v.take(14), cx + 3f, p.y + 12f, textPaint(DTEXT, 7.5f))
        }
        p.y += rowH
    }
    p.spacer(14f)

    p.clausula("Cláusula 1ª – Do Compromisso de Devolução no Prazo de 60 Dias")
    p.para("O CLIENTE compromete-se a devolver o(s) produto(s) ou efetivar a compra no prazo de 30 dias após o recebimento do orçamento. A não devolução no prazo legal de 60 dias implica responsabilidade pelo ICMS e demais encargos conforme o RICMS/SP.")

    p.clausula("Cláusula 2ª – Da Responsabilidade pelo ICMS")
    p.para("O CLIENTE responsabiliza-se pelo recolhimento do ICMS caso a devolução ultrapasse 60 dias.")

    p.clausula("Cláusula 3ª – Da Autorização de Emissão de Nota Fiscal")
    p.para("Em caso de dano ao produto, o CLIENTE autoriza a MEB a emitir Nota Fiscal de venda pelo valor da NFE original.")

    p.clausula("Cláusula 4ª – Do Inadimplemento")
    p.para("Havendo inadimplemento, incidirão multa de 2%, juros de 1% a.m. e correção pelo IPCA.")

    p.clausula("Cláusula 5ª – Da Irrevogabilidade")
    p.para("Este instrumento é irrevogável e irretratável.")

    p.clausula("Cláusula 7ª – Do Foro")
    p.para("As partes elegem o Foro da Comarca da Capital do Estado de São Paulo/SP.")

    p.spacer(10f)
    p.need(20f)
    p.canvas.drawText("${s.empLocalContrato.ifEmpty{"São Paulo"}}, $dataExtenso.",
        ML, p.y + 12f, textPaint(DTEXT, 9.5f, bold = true))
    p.spacer(30f)

    // Assinaturas
    p.need(100f)
    val sigW = CW / 2f - 20f
    
    // Assinatura Mitsubishi
    val xMEB = ML
    p.canvas.drawLine(xMEB, p.y + 40f, xMEB + sigW, p.y + 40f, rectPaint(DTEXT).also { it.strokeWidth = .5f })
    p.canvas.drawText("MITSUBISHI ELECTRIC DO BRASIL", xMEB, p.y + 52f, textPaint(DTEXT, 8.5f, bold = true))
    p.canvas.drawText("Thiago R M Caetano — CPF 309.733.308-80", xMEB, p.y + 64f, textPaint(DTEXT, 8.5f))

    // Assinatura Cliente (Campo 10)
    val xCL = ML + CW / 2f + 20f
    val clienteNome = s.empClienteNome.ifEmpty { s.assinaturaNome.ifEmpty { "_______________________" } }
    val clienteDoc  = s.empClienteCPF.ifEmpty { s.assinaturaDocumento.ifEmpty { "_______________________" } }
    
    if (s.signatureBase64.isNotEmpty()) {
        try {
            val bytes = Base64.decode(s.signatureBase64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) {
                val aspect = bmp.width.toFloat() / bmp.height.toFloat()
                val sw = sigW * 0.8f
                val sh = sw / aspect
                p.canvas.drawBitmap(bmp, null, RectF(xCL, p.y, xCL + sw, p.y + sh.coerceAtMost(40f)), null)
            }
        } catch (_: Exception) {}
    }
    
    p.canvas.drawLine(xCL, p.y + 40f, xCL + sigW, p.y + 40f, rectPaint(DTEXT).also { it.strokeWidth = .5f })
    p.canvas.drawText("CLIENTE (Campo 10)", xCL, p.y + 52f, textPaint(DTEXT, 8.5f, bold = true))
    p.canvas.drawText(clienteNome.uppercase(), xCL, p.y + 64f, textPaint(DTEXT, 8f))
    p.canvas.drawText("Doc: $clienteDoc", xCL, p.y + 74f, textPaint(DTEXT, 7.5f))

    p.y += 90f
    p.need(60f)
    listOf(
        "Testemunha 1: ${s.empTest1Nome.ifEmpty{"_______________________"}}\nCPF: ${s.empTest1CPF.ifEmpty{"_______________________"}}" to ML,
        "Testemunha 2: ${s.empTest2Nome.ifEmpty{"_______________________"}}\nCPF: ${s.empTest2CPF.ifEmpty{"_______________________"}}" to ML + CW / 2f + 20f
    ).forEach { (text, x) ->
        p.canvas.drawLine(x, p.y, x + sigW, p.y, rectPaint(DTEXT).also { it.strokeWidth = .5f; it.style = Paint.Style.STROKE })
        text.split("\n").forEachIndexed { i, line ->
            p.canvas.drawText(line, x, p.y + 14f + i * 14f, textPaint(DTEXT, 8.5f))
        }
    }

    p.finish()
    val safe = (s.empresa.ifEmpty { "cliente" }).replace(Regex("[^a-zA-Z0-9]"), "_")
    val file = File(context.cacheDir, "Contrato_Emprestimo_$safe.pdf")
    file.outputStream().use { doc.writeTo(it) }
    doc.close()
    return file
}

// ─── Compartilhar PDF ─────────────────────────────────────────────────────────

fun sharePdf(context: Context, file: File, title: String = "Compartilhar PDF") {
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, title))
}
