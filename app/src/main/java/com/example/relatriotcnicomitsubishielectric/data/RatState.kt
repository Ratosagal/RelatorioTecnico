package com.example.relatriotcnicomitsubishielectric.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

data class DayTime(
    val ciData: String = "", val ciHora: String = "",
    val coData: String = "", val coHora: String = "",
    val ipData: String = "", val ipHora: String = "",
    val fpData: String = "", val fpHora: String = "",
    val motivo: String = "Almoço",
    val just: String = ""
)

data class EmpProduto(
    val cod: String = "", val desc: String = "", val serie: String = "",
    val qtde: String = "1", val nf: String = "",
    val modeloMaq: String = "", val serieMaq: String = "", val data: String = ""
)

enum class PhotoField { INSTALADO, RETIRADO, RELATORIO }

data class RatState(
    // 1 – Marcação de ponto
    val isMultiDay: Boolean = false,
    val numDias: Int = 2,
    val dayTimes: List<DayTime> = listOf(DayTime()),

    // 2 – Dados do atendimento
    val numOS: String = "",
    val dataAtendimento: String = "",
    val prioridade: String = "Alta",
    val tecnico: String = "",
    val tipoTarefa: String = "Assistência Técnica",

    // 3 – Dados do cliente
    val empresa: String = "",
    val endereco: String = "",
    val contatoNome: String = "",
    val contatoEmail: String = "",
    val contatoTelefone: String = "",

    // 4 – Dados da máquina
    val fabricanteMaquina: String = "",
    val modeloMaquina: String = "",
    val serieMaquina: String = "",
    val modeloCNC: String = "",
    val serieCNC: String = "",
    val maquinaParada: String = "",
    val maquinaGarantia: String = "",

    // 5 – Diagnóstico
    val problema: String = "",
    val causa: String = "",
    val solucao: String = "",

    // 6 – Equipamentos instalados
    val equipInstalado: String = "",
    val descInstalado: String = "",
    val fotosInstalado: List<String> = emptyList(),
    val emprestimo: String = "",

    // Contrato de empréstimo
    val empCNPJ: String = "", val empCidade: String = "",
    val empEstado: String = "", val empCEP: String = "",
    val empEndereco: String = "",
    val empRepNome: String = "", val empRepCidadania: String = "",
    val empRepEstCivil: String = "", val empRepProfissao: String = "",
    val empRepCPF: String = "", val empRepRG: String = "",
    val empProdutos: List<EmpProduto> = listOf(EmpProduto()),
    val empDataContrato: String = "", val empLocalContrato: String = "",
    val empClienteNome: String = "", val empClienteCPF: String = "",
    val empTest1Nome: String = "", val empTest1CPF: String = "",
    val empTest2Nome: String = "", val empTest2CPF: String = "",

    // 7 – Equipamentos retirados
    val equipRetirado: String = "",
    val descRetirado: String = "",
    val condicaoRetirado: String = "",
    val fotosRetirado: List<String> = emptyList(),

    // 8 – Fotos gerais
    val fotosRelatorio: List<String> = emptyList(),

    // 9 – Lab
    val labInfo: String = "",

    // 10 – Assinatura
    val assinaturaNome: String = "",
    val assinaturaDocumento: String = "",
    val signatureBase64: String = ""
)

// ── Utilidades de tempo ──────────────────────────────────────────────────────

private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

fun combineDT(date: String, time: String): Long? {
    if (date.isEmpty() || time.isEmpty()) return null
    return try { sdf.parse("$date $time")?.time } catch (e: Exception) { null }
}

fun fmtDuration(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalMin = (ms / 60000).coerceAtLeast(0)
    return "${(totalMin / 60).pad()}:${(totalMin % 60).pad()}"
}

fun Long.pad() = toString().padStart(2, '0')

fun calcDay(day: DayTime): Pair<String, String> {
    val ci = combineDT(day.ciData, day.ciHora)
    val co = combineDT(day.coData, day.coHora)
    val ip = combineDT(day.ipData, day.ipHora)
    val fp = combineDT(day.fpData, day.fpHora)
    val pausa = if (ip != null && fp != null) maxOf(0L, fp - ip) else 0L
    val total = if (ci != null && co != null) maxOf(0L, (co - ci) - pausa) else 0L
    return fmtDuration(pausa) to fmtDuration(total)
}

fun calcGrandTotal(days: List<DayTime>): Pair<String, String> {
    var pausaMs = 0L; var totalMs = 0L
    days.forEach { d ->
        val ci = combineDT(d.ciData, d.ciHora)
        val co = combineDT(d.coData, d.coHora)
        val ip = combineDT(d.ipData, d.ipHora)
        val fp = combineDT(d.fpData, d.fpHora)
        val p = if (ip != null && fp != null) maxOf(0L, fp - ip) else 0L
        val t = if (ci != null && co != null) maxOf(0L, (co - ci) - p) else 0L
        pausaMs += p; totalMs += t
    }
    return fmtDuration(pausaMs) to fmtDuration(totalMs)
}

// ── Persistência JSON ─────────────────────────────────────────────────────────

fun DayTime.toJson() = JSONObject().apply {
    put("ciData", ciData); put("ciHora", ciHora)
    put("coData", coData); put("coHora", coHora)
    put("ipData", ipData); put("ipHora", ipHora)
    put("fpData", fpData); put("fpHora", fpHora)
    put("motivo", motivo); put("just", just)
}

fun JSONObject.toDayTime() = DayTime(
    ciData = optString("ciData"), ciHora = optString("ciHora"),
    coData = optString("coData"), coHora = optString("coHora"),
    ipData = optString("ipData"), ipHora = optString("ipHora"),
    fpData = optString("fpData"), fpHora = optString("fpHora"),
    motivo = optString("motivo", "Almoço"),
    just = optString("just")
)

fun EmpProduto.toJson() = JSONObject().apply {
    put("cod", cod); put("desc", desc); put("serie", serie)
    put("qtde", qtde); put("nf", nf)
    put("modeloMaq", modeloMaq); put("serieMaq", serieMaq); put("data", data)
}

fun JSONObject.toEmpProduto() = EmpProduto(
    cod = optString("cod"), desc = optString("desc"), serie = optString("serie"),
    qtde = optString("qtde", "1"), nf = optString("nf"),
    modeloMaq = optString("modeloMaq"), serieMaq = optString("serieMaq"),
    data = optString("data")
)

fun RatState.toJson(): String {
    val j = JSONObject()
    j.put("isMultiDay", isMultiDay); j.put("numDias", numDias)
    val dts = JSONArray(); dayTimes.forEach { dts.put(it.toJson()) }; j.put("dayTimes", dts)
    j.put("numOS", numOS); j.put("dataAtendimento", dataAtendimento)
    j.put("prioridade", prioridade); j.put("tecnico", tecnico); j.put("tipoTarefa", tipoTarefa)
    j.put("empresa", empresa); j.put("endereco", endereco)
    j.put("contatoNome", contatoNome); j.put("contatoEmail", contatoEmail); j.put("contatoTelefone", contatoTelefone)
    j.put("fabricanteMaquina", fabricanteMaquina); j.put("modeloMaquina", modeloMaquina)
    j.put("serieMaquina", serieMaquina); j.put("modeloCNC", modeloCNC); j.put("serieCNC", serieCNC)
    j.put("maquinaParada", maquinaParada); j.put("maquinaGarantia", maquinaGarantia)
    j.put("problema", problema); j.put("causa", causa); j.put("solucao", solucao)
    j.put("equipInstalado", equipInstalado); j.put("descInstalado", descInstalado)
    val fi = JSONArray(); fotosInstalado.forEach { fi.put(it) }; j.put("fotosInstalado", fi)
    j.put("emprestimo", emprestimo)
    j.put("empCNPJ", empCNPJ); j.put("empCidade", empCidade); j.put("empEstado", empEstado)
    j.put("empCEP", empCEP); j.put("empEndereco", empEndereco)
    j.put("empRepNome", empRepNome); j.put("empRepCidadania", empRepCidadania)
    j.put("empRepEstCivil", empRepEstCivil); j.put("empRepProfissao", empRepProfissao)
    j.put("empRepCPF", empRepCPF); j.put("empRepRG", empRepRG)
    val ep = JSONArray(); empProdutos.forEach { ep.put(it.toJson()) }; j.put("empProdutos", ep)
    j.put("empDataContrato", empDataContrato); j.put("empLocalContrato", empLocalContrato)
    j.put("empClienteNome", empClienteNome); j.put("empClienteCPF", empClienteCPF)
    j.put("empTest1Nome", empTest1Nome); j.put("empTest1CPF", empTest1CPF)
    j.put("empTest2Nome", empTest2Nome); j.put("empTest2CPF", empTest2CPF)
    j.put("equipRetirado", equipRetirado); j.put("descRetirado", descRetirado)
    j.put("condicaoRetirado", condicaoRetirado)
    val fr = JSONArray(); fotosRetirado.forEach { fr.put(it) }; j.put("fotosRetirado", fr)
    val frel = JSONArray(); fotosRelatorio.forEach { frel.put(it) }; j.put("fotosRelatorio", frel)
    j.put("labInfo", labInfo)
    j.put("assinaturaNome", assinaturaNome); j.put("assinaturaDocumento", assinaturaDocumento)
    j.put("signatureBase64", signatureBase64)
    return j.toString()
}

fun ratStateFromJson(raw: String): RatState {
    val j = JSONObject(raw)
    fun JSONObject.arr(key: String) = optJSONArray(key) ?: JSONArray()
    fun JSONArray.strings() = (0 until length()).map { getString(it) }
    val dts = j.arr("dayTimes").let { a -> (0 until a.length()).map { a.getJSONObject(it).toDayTime() } }
        .ifEmpty { listOf(DayTime()) }
    val eps = j.arr("empProdutos").let { a -> (0 until a.length()).map { a.getJSONObject(it).toEmpProduto() } }
        .ifEmpty { listOf(EmpProduto()) }
    return RatState(
        isMultiDay = j.optBoolean("isMultiDay"), numDias = j.optInt("numDias", 2),
        dayTimes = dts,
        numOS = j.optString("numOS"), dataAtendimento = j.optString("dataAtendimento"),
        prioridade = j.optString("prioridade", "Alta"), tecnico = j.optString("tecnico"),
        tipoTarefa = j.optString("tipoTarefa", "Assistência Técnica"),
        empresa = j.optString("empresa"), endereco = j.optString("endereco"),
        contatoNome = j.optString("contatoNome"), contatoEmail = j.optString("contatoEmail"),
        contatoTelefone = j.optString("contatoTelefone"),
        fabricanteMaquina = j.optString("fabricanteMaquina"), modeloMaquina = j.optString("modeloMaquina"),
        serieMaquina = j.optString("serieMaquina"), modeloCNC = j.optString("modeloCNC"),
        serieCNC = j.optString("serieCNC"),
        maquinaParada = j.optString("maquinaParada"), maquinaGarantia = j.optString("maquinaGarantia"),
        problema = j.optString("problema"), causa = j.optString("causa"), solucao = j.optString("solucao"),
        equipInstalado = j.optString("equipInstalado"), descInstalado = j.optString("descInstalado"),
        fotosInstalado = j.arr("fotosInstalado").strings(),
        emprestimo = j.optString("emprestimo"),
        empCNPJ = j.optString("empCNPJ"), empCidade = j.optString("empCidade"),
        empEstado = j.optString("empEstado"), empCEP = j.optString("empCEP"),
        empEndereco = j.optString("empEndereco"), empRepNome = j.optString("empRepNome"),
        empRepCidadania = j.optString("empRepCidadania"), empRepEstCivil = j.optString("empRepEstCivil"),
        empRepProfissao = j.optString("empRepProfissao"), empRepCPF = j.optString("empRepCPF"),
        empRepRG = j.optString("empRepRG"),
        empProdutos = eps,
        empDataContrato = j.optString("empDataContrato"), empLocalContrato = j.optString("empLocalContrato"),
        empClienteNome = j.optString("empClienteNome"), empClienteCPF = j.optString("empClienteCPF"),
        empTest1Nome = j.optString("empTest1Nome"), empTest1CPF = j.optString("empTest1CPF"),
        empTest2Nome = j.optString("empTest2Nome"), empTest2CPF = j.optString("empTest2CPF"),
        equipRetirado = j.optString("equipRetirado"), descRetirado = j.optString("descRetirado"),
        condicaoRetirado = j.optString("condicaoRetirado"),
        fotosRetirado = j.arr("fotosRetirado").strings(),
        fotosRelatorio = j.arr("fotosRelatorio").strings(),
        labInfo = j.optString("labInfo"),
        assinaturaNome = j.optString("assinaturaNome"), assinaturaDocumento = j.optString("assinaturaDocumento"),
        signatureBase64 = j.optString("signatureBase64")
    )
}
