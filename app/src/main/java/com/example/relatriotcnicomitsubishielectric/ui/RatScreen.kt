package com.example.relatriotcnicomitsubishielectric.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.relatriotcnicomitsubishielectric.RatViewModel
import com.example.relatriotcnicomitsubishielectric.data.*
import com.example.relatriotcnicomitsubishielectric.report.generateContrato
import com.example.relatriotcnicomitsubishielectric.report.generateRelatorio
import com.example.relatriotcnicomitsubishielectric.report.sharePdf
import com.example.relatriotcnicomitsubishielectric.ui.components.*
import com.example.relatriotcnicomitsubishielectric.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RatScreen(vm: RatViewModel) {
    val s by vm.state.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpar formulário") },
            text = { Text("Tem certeza? Esta ação apaga todos os dados preenchidos.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.clearAll(ctx); showClearDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MitsubishiRed)
                ) { Text("Limpar tudo") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = { RatTopBar() },
        bottomBar = {
            RatBottomBar(
                onClear = { showClearDialog = true },
                onRelatorio = {
                    if (s.empresa.isEmpty() || s.tecnico.isEmpty()) {
                        scope.launch { snack.showSnackbar("Preencha ao menos Empresa e Tecnico.") }
                    } else {
                        scope.launch {
                            val file = withContext(Dispatchers.IO) { generateRelatorio(ctx, s) }
                            sharePdf(ctx, file, "Compartilhar Relatorio")
                        }
                    }
                },
                onContrato = {
                    if (s.emprestimo != "Sim - gerar contrato") {
                        scope.launch { snack.showSnackbar("Marque 'Sim - gerar contrato' na secao 6.") }
                    } else if (s.empresa.isEmpty()) {
                        scope.launch { snack.showSnackbar("Preencha o nome da empresa (secao 3).") }
                    } else {
                        scope.launch {
                            val file = withContext(Dispatchers.IO) { generateContrato(ctx, s) }
                            sharePdf(ctx, file, "Compartilhar Contrato")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp,
                top = pad.calculateTopPadding() + 12.dp,
                bottom = pad.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Section1Ponto(vm, s) }
            item { Section2Atendimento(vm, s) }
            item { Section3Cliente(vm, s) }
            item { Section4Maquina(vm, s) }
            item { Section5Diagnostico(vm, s) }
            item { Section6Instalado(vm, s) }
            item { Section7Retirado(vm, s) }
            item { Section8Fotos(vm, s) }
            item { Section9Lab(vm, s) }
            item { Section10Assinatura(vm, s) }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun RatTopBar() {
    Surface(color = Steel, shadowElevation = 6.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MitsubishiLogo(size = 36.dp)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Relatório de Assistência Técnica",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "CNC / Automação Industrial",
                    color = Color(0xFFC9D2DA),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun MitsubishiLogo(size: androidx.compose.ui.unit.Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = com.example.relatriotcnicomitsubishielectric.R.drawable.ic_mitsubishi_logo),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(size)
        )
        Spacer(Modifier.width(size / 4))
        Column {
            Text(
                "MITSUBISHI",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.4).sp,
                lineHeight = (size.value * 0.4).sp,
                letterSpacing = (-0.2).sp
            )
            Text(
                "ELECTRIC",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.4).sp,
                lineHeight = (size.value * 0.4).sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────

@Composable
private fun RatBottomBar(
    onClear: () -> Unit,
    onRelatorio: () -> Unit,
    onContrato: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onClear,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, LineGray),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Limpar", fontWeight = FontWeight.SemiBold, color = Steel)
            }
            Button(
                onClick = onContrato,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C4A78)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("Contrato", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Button(
                onClick = onRelatorio,
                colors = ButtonDefaults.buttonColors(containerColor = MitsubishiRed),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("Relatorio PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ─── Seção 1 — Marcação de Ponto ─────────────────────────────────────────────

@Composable
private fun Section1Ponto(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(number = 1, title = "Marcacao de Ponto") {

        // Toggle multi-dia
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F4FF), RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = s.isMultiDay,
                onCheckedChange = { vm.setMultiDay(it, ctx) },
                colors = CheckboxDefaults.colors(checkedColor = MitsubishiRed)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Atendimento com mais de 1 dia",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            if (s.isMultiDay) {
                OutlinedTextField(
                    value = s.numDias.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { vm.setNumDias(it, ctx) } },
                    modifier = Modifier.width(64.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MitsubishiRed,
                        unfocusedBorderColor = LineGray
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("dias", fontSize = 14.sp, color = SteelLight)
            }
        }

        Spacer(Modifier.height(14.dp))

        s.dayTimes.forEachIndexed { idx, day ->
            DayCard(
                idx = idx,
                day = day,
                showLabel = s.isMultiDay,
                onUpdate = { updated -> vm.updateDay(idx, updated, ctx) }
            )
            if (idx < s.dayTimes.size - 1) Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(14.dp))
        val (gp, gt) = calcGrandTotal(s.dayTimes)
        TotalBar(
            pausaLabel = if (s.dayTimes.size > 1) "Pausa total (todos os dias)" else "Total em pausa",
            pausa = gp,
            totalLabel = if (s.dayTimes.size > 1) "Horas trabalhadas (total)" else "Total no atendimento",
            total = gt
        )
        Spacer(Modifier.height(10.dp))
        HintBlock("Preencha os campos ou toque em 'Agora' para registrar o horario atual.")
    }
}

@Composable
private fun DayCard(idx: Int, day: DayTime, showLabel: Boolean, onUpdate: (DayTime) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAF8), RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        if (showLabel) {
            Box(
                modifier = Modifier
                    .background(Steel, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    "DIA ${idx + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        DateTimeNowRow(
            "Check-in — inicio do atendimento",
            day.ciData, day.ciHora,
            onDate = { onUpdate(day.copy(ciData = it)) },
            onTime = { onUpdate(day.copy(ciHora = it)) }
        )
        Spacer(Modifier.height(10.dp))
        DateTimeNowRow(
            "Check-out — fim do atendimento",
            day.coData, day.coHora,
            onDate = { onUpdate(day.copy(coData = it)) },
            onTime = { onUpdate(day.copy(coHora = it)) }
        )
        Spacer(Modifier.height(10.dp))
        DateTimeNowRow(
            "Inicio da pausa",
            day.ipData, day.ipHora,
            onDate = { onUpdate(day.copy(ipData = it)) },
            onTime = { onUpdate(day.copy(ipHora = it)) }
        )
        Spacer(Modifier.height(10.dp))
        DateTimeNowRow(
            "Fim da pausa",
            day.fpData, day.fpHora,
            onDate = { onUpdate(day.copy(fpData = it)) },
            onTime = { onUpdate(day.copy(fpHora = it)) }
        )
        Spacer(Modifier.height(12.dp))

        RatDropdown(
            "Motivo da pausa",
            day.motivo,
            listOf("Almoco", "Deslocamento", "Outro"),
            { onUpdate(day.copy(motivo = it)) }
        )
        Spacer(Modifier.height(10.dp))
        RatField(
            "Justificativa (atraso / observacao)",
            day.just,
            { onUpdate(day.copy(just = it)) },
            placeholder = "opcional"
        )

        Spacer(Modifier.height(12.dp))
        val (dp, dt) = calcDay(day)
        TimeSummaryRow(pausa = dp, total = dt)
    }
}

// ─── Seção 2 — Dados do Atendimento ──────────────────────────────────────────

@Composable
private fun Section2Atendimento(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(2, "Dados do Atendimento") {
        RatField(
            "Numero da tarefa / OS",
            s.numOS,
            { vm.setNumOS(it, ctx) },
            placeholder = "ex: 75553269",
            monospace = true
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateField(
                "Data do atendimento",
                s.dataAtendimento,
                { vm.setDataAtendimento(it, ctx) },
                modifier = Modifier.weight(1.3f)
            )
            RatDropdown(
                "Prioridade",
                s.prioridade,
                listOf("Baixa", "Media", "Alta", "Urgente"),
                { vm.setPrioridade(it, ctx) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        RatField(
            "Tecnico responsavel *",
            s.tecnico,
            { vm.setTecnico(it, ctx) },
            placeholder = "Nome completo do tecnico"
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "Tipo de tarefa",
            s.tipoTarefa,
            { vm.setTipoTarefa(it, ctx) }
        )
    }
}

// ─── Seção 3 — Dados do Cliente ──────────────────────────────────────────────

@Composable
private fun Section3Cliente(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(3, "Dados do Cliente") {
        RatField(
            "Empresa *",
            s.empresa,
            { vm.setEmpresa(it, ctx) },
            placeholder = "Razao social da empresa"
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "Endereco do atendimento",
            s.endereco,
            { vm.setEndereco(it, ctx) },
            placeholder = "Rua, numero, bairro, cidade"
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "Nome do contato *",
            s.contatoNome,
            { vm.setContatoNome(it, ctx) }
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "E-mail do contato",
            s.contatoEmail,
            { vm.setContatoEmail(it, ctx) }
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "Telefone",
            s.contatoTelefone,
            { vm.setContatoTelefone(it, ctx) },
            placeholder = "(11) 99999-9999"
        )
    }
}

// ─── Seção 4 — Dados da Máquina ──────────────────────────────────────────────

@Composable
private fun Section4Maquina(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(4, "Dados da Maquina") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RatField(
                "Fabricante",
                s.fabricanteMaquina,
                { vm.setFabricanteMaquina(it, ctx) },
                modifier = Modifier.weight(1f)
            )
            RatField(
                "Modelo da maquina",
                s.modeloMaquina,
                { vm.setModeloMaquina(it, ctx) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        RatField(
            "Numero de serie (maquina)",
            s.serieMaquina,
            { vm.setSerieMaquina(it, ctx) },
            monospace = true
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RatField(
                "Modelo do CNC",
                s.modeloCNC,
                { vm.setModeloCNC(it, ctx) },
                modifier = Modifier.weight(1f)
            )
            RatField(
                "Serie do CNC",
                s.serieCNC,
                { vm.setSerieCNC(it, ctx) },
                modifier = Modifier.weight(1f),
                monospace = true
            )
        }
        Spacer(Modifier.height(14.dp))
        RadioGroup(
            "Maquina estava parada?",
            listOf("Sim", "Nao"),
            s.maquinaParada,
            { vm.setMaquinaParada(it, ctx) }
        )
        Spacer(Modifier.height(12.dp))
        RadioGroup(
            "Maquina em garantia?",
            listOf("Sim", "Nao"),
            s.maquinaGarantia,
            { vm.setMaquinaGarantia(it, ctx) }
        )
    }
}

// ─── Seção 5 — Diagnóstico ───────────────────────────────────────────────────

@Composable
private fun Section5Diagnostico(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(5, "Diagnostico") {
        RatField(
            "Defeito / Problema relatado",
            s.problema,
            { vm.setProblema(it, ctx) },
            placeholder = "Descreva o problema relatado pelo cliente e/ou constatado em campo",
            singleLine = false,
            minLines = 3,
            maxLines = 8
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "Causa raiz",
            s.causa,
            { vm.setCausa(it, ctx) },
            placeholder = "Causa identificada",
            singleLine = false,
            minLines = 2,
            maxLines = 6
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "Solucao aplicada",
            s.solucao,
            { vm.setSolucao(it, ctx) },
            placeholder = "Descreva o que foi feito para resolver o problema",
            singleLine = false,
            minLines = 2,
            maxLines = 6
        )
    }
}

// ─── Seção 6 — Equipamentos Instalados ───────────────────────────────────────

@Composable
private fun Section6Instalado(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(6, "Equipamentos Instalados") {
        RadioGroup(
            "Foi instalado algum equipamento?",
            listOf("Sim", "Nao"),
            s.equipInstalado,
            { vm.setEquipInstalado(it, ctx) }
        )

        if (s.equipInstalado == "Sim") {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = LineGray)
            Spacer(Modifier.height(14.dp))

            RatField(
                "Descricao dos equipamentos instalados",
                s.descInstalado,
                { vm.setDescInstalado(it, ctx) },
                placeholder = "Ex: PD27 / no serie / emprestimo",
                singleLine = false,
                minLines = 2
            )
            Spacer(Modifier.height(14.dp))

            FieldLabel("FOTOS DO EQUIPAMENTO INSTALADO")
            Spacer(Modifier.height(8.dp))
            PhotoSection(vm = vm, photos = s.fotosInstalado, field = PhotoField.INSTALADO)

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(thickness = 2.dp, color = LineGray)
            Spacer(Modifier.height(14.dp))

            RadioGroup(
                "Este equipamento e em emprestimo / demonstracao?",
                listOf("Sim - gerar contrato", "Nao"),
                s.emprestimo,
                { vm.setEmprestimo(it, ctx) }
            )

            if (s.emprestimo == "Sim - gerar contrato") {
                Spacer(Modifier.height(16.dp))
                SubSectionLabel("Contrato de Emprestimo / Demonstracao")
                Spacer(Modifier.height(16.dp))

                SubSectionTitle("Dados do cliente (empresa)")
                RatField(
                    "CNPJ *",
                    s.empCNPJ,
                    { vm.setEmpCNPJ(it, ctx) },
                    placeholder = "00.000.000/0000-00",
                    monospace = true
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatField(
                        "Cidade *",
                        s.empCidade,
                        { vm.setEmpCidade(it, ctx) },
                        modifier = Modifier.weight(2f)
                    )
                    RatField(
                        "UF",
                        s.empEstado,
                        { vm.setEmpEstado(it, ctx) },
                        modifier = Modifier.weight(1f),
                        placeholder = "SP"
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatField(
                        "CEP",
                        s.empCEP,
                        { vm.setEmpCEP(it, ctx) },
                        modifier = Modifier.weight(1f),
                        placeholder = "00000-000"
                    )
                    RatField(
                        "Endereco",
                        s.empEndereco,
                        { vm.setEmpEndereco(it, ctx) },
                        modifier = Modifier.weight(2f)
                    )
                }

                SubSectionTitle("Representante legal do cliente")
                RatField(
                    "Nome do representante *",
                    s.empRepNome,
                    { vm.setEmpRepNome(it, ctx) }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatField(
                        "Cidadania",
                        s.empRepCidadania,
                        { vm.setEmpRepCidadania(it, ctx) },
                        modifier = Modifier.weight(1f),
                        placeholder = "brasileiro(a)"
                    )
                    RatField(
                        "Estado civil",
                        s.empRepEstCivil,
                        { vm.setEmpRepEstCivil(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                RatField(
                    "Profissao",
                    s.empRepProfissao,
                    { vm.setEmpRepProfissao(it, ctx) }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatField(
                        "CPF",
                        s.empRepCPF,
                        { vm.setEmpRepCPF(it, ctx) },
                        modifier = Modifier.weight(1f),
                        placeholder = "000.000.000-00"
                    )
                    RatField(
                        "RG",
                        s.empRepRG,
                        { vm.setEmpRepRG(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                }

                SubSectionTitle("Tabela A - Produto(s) em emprestimo")
                s.empProdutos.forEachIndexed { i, prod ->
                    EmpProdutoRow(
                        idx = i,
                        prod = prod,
                        onUpdate = { vm.updateEmpProduto(i, it, ctx) },
                        onRemove = { vm.removeEmpProduto(i, ctx) },
                        canRemove = s.empProdutos.size > 1
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = { vm.addEmpProduto(ctx) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Steel)
                ) {
                    Icon(Icons.Default.Add, null, tint = Steel)
                    Spacer(Modifier.width(6.dp))
                    Text("Adicionar produto", color = Steel, fontWeight = FontWeight.Bold)
                }

                SubSectionTitle("Data e local do contrato")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField(
                        "Data do contrato",
                        s.empDataContrato,
                        { vm.setEmpDataContrato(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                    RatField(
                        "Local (cidade)",
                        s.empLocalContrato,
                        { vm.setEmpLocalContrato(it, ctx) },
                        modifier = Modifier.weight(1f),
                        placeholder = "Sao Paulo"
                    )
                }
                Spacer(Modifier.height(10.dp))
                RatField(
                    "Nome do signatario (cliente)",
                    s.empClienteNome,
                    { vm.setEmpClienteNome(it, ctx) }
                )
                Spacer(Modifier.height(10.dp))
                RatField(
                    "CPF do signatario",
                    s.empClienteCPF,
                    { vm.setEmpClienteCPF(it, ctx) },
                    placeholder = "000.000.000-00"
                )

                SubSectionTitle("Testemunhas")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatField(
                        "Testemunha 1 - Nome",
                        s.empTest1Nome,
                        { vm.setEmpTest1Nome(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                    RatField(
                        "CPF",
                        s.empTest1CPF,
                        { vm.setEmpTest1CPF(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RatField(
                        "Testemunha 2 - Nome",
                        s.empTest2Nome,
                        { vm.setEmpTest2Nome(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                    RatField(
                        "CPF",
                        s.empTest2CPF,
                        { vm.setEmpTest2CPF(it, ctx) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmpProdutoRow(
    idx: Int,
    prod: EmpProduto,
    onUpdate: (EmpProduto) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F2), RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Produto ${idx + 1}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Steel,
                modifier = Modifier.weight(1f)
            )
            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = MitsubishiRed, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        RatField("Codigo", prod.cod, { onUpdate(prod.copy(cod = it)) }, placeholder = "cod.")
        Spacer(Modifier.height(8.dp))
        RatField("Descricao", prod.desc, { onUpdate(prod.copy(desc = it)) })
        Spacer(Modifier.height(8.dp))
        RatField("Numero de serie", prod.serie, { onUpdate(prod.copy(serie = it)) }, monospace = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RatField("Qtde", prod.qtde, { onUpdate(prod.copy(qtde = it)) }, modifier = Modifier.weight(1f))
            RatField("No NF", prod.nf, { onUpdate(prod.copy(nf = it)) }, modifier = Modifier.weight(2f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RatField(
                "Modelo maquina",
                prod.modeloMaq,
                { onUpdate(prod.copy(modeloMaq = it)) },
                modifier = Modifier.weight(1f)
            )
            RatField(
                "Serie maquina",
                prod.serieMaq,
                { onUpdate(prod.copy(serieMaq = it)) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        DateField("Data", prod.data, { onUpdate(prod.copy(data = it)) })
    }
}

// ─── Seção 7 — Equipamentos Retirados ────────────────────────────────────────

@Composable
private fun Section7Retirado(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(7, "Equipamentos Retirados") {
        RadioGroup(
            "Foi retirado algum equipamento?",
            listOf("Sim", "Nao"),
            s.equipRetirado,
            { vm.setEquipRetirado(it, ctx) }
        )
        if (s.equipRetirado == "Sim") {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = LineGray)
            Spacer(Modifier.height(14.dp))
            RatField(
                "Descricao dos equipamentos retirados",
                s.descRetirado,
                { vm.setDescRetirado(it, ctx) },
                placeholder = "Ex: FCU7-YZ141 / no serie / reparo",
                singleLine = false,
                minLines = 2
            )
            Spacer(Modifier.height(12.dp))
            RatField(
                "Condicao do equipamento",
                s.condicaoRetirado,
                { vm.setCondicaoRetirado(it, ctx) },
                placeholder = "Ex: queimado, com defeito, para reparo"
            )
            Spacer(Modifier.height(14.dp))
            FieldLabel("FOTOS DO EQUIPAMENTO RETIRADO")
            Spacer(Modifier.height(8.dp))
            PhotoSection(vm = vm, photos = s.fotosRetirado, field = PhotoField.RETIRADO)
        }
    }
}

// ─── Seção 8 — Fotos Gerais ───────────────────────────────────────────────────

@Composable
private fun Section8Fotos(vm: RatViewModel, s: RatState) {
    SectionCard(8, "Fotos do Relatorio") {
        PhotoSection(vm = vm, photos = s.fotosRelatorio, field = PhotoField.RELATORIO)
    }
}

// ─── Seção 9 — Laboratório ────────────────────────────────────────────────────

@Composable
private fun Section9Lab(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(9, "Informacoes para o Laboratorio") {
        RatField(
            "",
            s.labInfo,
            { vm.setLabInfo(it, ctx) },
            placeholder = "Descricao de falhas, pecas a reparar, observacoes tecnicas...",
            singleLine = false,
            minLines = 4,
            maxLines = 12
        )
    }
}

// ─── Seção 10 — Assinatura ────────────────────────────────────────────────────

@Composable
private fun Section10Assinatura(vm: RatViewModel, s: RatState) {
    val ctx = LocalContext.current
    SectionCard(10, "Assinatura do Cliente") {
        RatField(
            "Nome do signatario",
            s.assinaturaNome,
            { vm.setAssinaturaNome(it, ctx) }
        )
        Spacer(Modifier.height(12.dp))
        RatField(
            "CPF / Documento",
            s.assinaturaDocumento,
            { vm.setAssinaturaDocumento(it, ctx) }
        )
        Spacer(Modifier.height(16.dp))
        FieldLabel("ASSINE NO CAMPO ABAIXO")
        Spacer(Modifier.height(8.dp))
        SignatureCanvas(
            savedBase64 = s.signatureBase64,
            onChanged = { bitmap -> vm.saveSignature(bitmap, ctx) },
            onClear = { vm.saveSignature(null, ctx) }
        )
        Spacer(Modifier.height(4.dp))
        Text("Use o dedo para assinar", fontSize = 13.sp, color = SteelLight)
        Spacer(Modifier.height(10.dp))
        HintBlock("Tudo e salvo automaticamente. Nenhum dado e enviado para a internet.")
    }
}

// ─── Utilitário local ─────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
        color = SteelLight
    )
}
