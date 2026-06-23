package com.example.relatriotcnicomitsubishielectric.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.relatriotcnicomitsubishielectric.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Seção card com cabeçalho numerado ────────────────────────────────────────

@Composable
fun SectionCard(
    number: Int,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF2F0EB))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 28.dp, minHeight = 28.dp)
                    .background(MitsubishiRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = Steel
            )
        }
        HorizontalDivider(color = LineGray, thickness = 1.dp)
        // Corpo
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

// ── Campo de texto com label ──────────────────────────────────────────────────

@Composable
fun RatField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    monospace: Boolean = false
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkDark,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 15.sp, color = SteelLight.copy(alpha = .5f)) },
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MitsubishiRed,
                unfocusedBorderColor = LineGray,
                cursorColor = MitsubishiRed
            ),
            shape = RoundedCornerShape(6.dp)
        )
    }
}

// ── Dropdown / Select ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkDark,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MitsubishiRed,
                    unfocusedBorderColor = LineGray
                ),
                shape = RoundedCornerShape(6.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { onValueChange(opt); expanded = false }
                    )
                }
            }
        }
    }
}

// ── Radio group (Sim/Não ou opções) ──────────────────────────────────────────

@Composable
fun RadioGroup(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkDark,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { opt ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = opt == selected,
                            onClick = { onSelect(opt) },
                            role = Role.RadioButton
                        )
                        .padding(end = 12.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = opt == selected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = MitsubishiRed)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(opt, fontSize = 15.sp, color = InkDark)
                }
            }
        }
    }
}

// ── Seletor de Data ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: String,        // formato interno: "yyyy-MM-dd"
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    // displayText mantém o que o usuário vê (DD/MM/AAAA).
    // Quando `value` muda externamente (Agora, picker, limpar), reinicia o display.
    fun internalToDisplay(v: String): String {
        if (v.isEmpty()) return ""
        val p = v.split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else v
    }
    var displayText by remember(value) { mutableStateOf(internalToDisplay(value)) }

    // Picker — iniciado a partir do valor interno atual
    val parseSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val initMs = remember(value) {
        if (value.isEmpty()) null
        else try { parseSdf.parse(value)?.time } catch (_: Exception) { null }
    }
    val dpState = rememberDatePickerState(initialSelectedDateMillis = initMs, yearRange = 2020..2040)

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { ms ->
                        val utcSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        utcSdf.timeZone = TimeZone.getTimeZone("UTC")
                        onValueChange(utcSdf.format(Date(ms)))
                    }
                    showPicker = false
                }) { Text("OK", color = MitsubishiRed) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dpState) }
    }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkDark,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        OutlinedTextField(
            value = displayText,
            onValueChange = { input ->
                // Extrai só dígitos, máximo 8 (DDMMAAAA)
                val digits = input.filter { it.isDigit() }.take(8)
                // Auto-formata como DD/MM/AAAA
                val formatted = buildString {
                    digits.forEachIndexed { i, c ->
                        if (i == 2 || i == 4) append('/')
                        append(c)
                    }
                }
                displayText = formatted
                // Converte para formato interno apenas quando a data está completa
                if (formatted.length == 10) {
                    val parts = formatted.split("/")
                    if (parts.size == 3) {
                        onValueChange("${parts[2]}-${parts[1]}-${parts[0]}")
                    }
                } else if (formatted.isEmpty()) {
                    onValueChange("")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { showPicker = true }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.DateRange, null, tint = SteelLight, modifier = Modifier.size(20.dp))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MitsubishiRed, unfocusedBorderColor = LineGray,
                cursorColor = MitsubishiRed
            ),
            shape = RoundedCornerShape(6.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
            placeholder = { Text("DD/MM/AAAA", color = SteelLight.copy(.5f), fontSize = 15.sp) }
        )
    }
}

// ── Seletor de Hora ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(
    label: String,
    value: String,        // formato "HH:MM" ou ""
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val initH = remember(value) { value.split(":").getOrNull(0)?.toIntOrNull() ?: 0 }
    val initM = remember(value) { value.split(":").getOrNull(1)?.toIntOrNull() ?: 0 }
    val tpState = rememberTimePickerState(initialHour = initH, initialMinute = initM, is24Hour = true)

    if (showPicker) {
        Dialog(onDismissRequest = { showPicker = false }) {
            Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 8.dp) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Selecione o horario",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    TimePicker(
                        state = tpState,
                        colors = TimePickerDefaults.colors(
                            clockDialSelectedContentColor = Color.White,
                            clockDialColor = Paper,
                            selectorColor = MitsubishiRed,
                            timeSelectorSelectedContainerColor = MitsubishiRed,
                            timeSelectorSelectedContentColor = Color.White
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
                        TextButton(onClick = {
                            val h = tpState.hour.toString().padStart(2, '0')
                            val m = tpState.minute.toString().padStart(2, '0')
                            onValueChange("$h:$m")
                            showPicker = false
                        }) { Text("OK", color = MitsubishiRed) }
                    }
                }
            }
        }
    }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkDark,
                modifier = Modifier.padding(bottom = 5.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                // Extrai só dígitos, máximo 4 (HHMM)
                val digits = input.filter { it.isDigit() }.take(4)
                // Auto-formata como HH:MM
                val formatted = when {
                    digits.length <= 2 -> digits
                    else -> "${digits.take(2)}:${digits.drop(2)}"
                }
                onValueChange(formatted)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { showPicker = true }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Schedule, null, tint = SteelLight, modifier = Modifier.size(20.dp))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MitsubishiRed, unfocusedBorderColor = LineGray,
                cursorColor = MitsubishiRed
            ),
            shape = RoundedCornerShape(6.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
            placeholder = { Text("HH:MM", color = SteelLight.copy(.5f), fontSize = 15.sp) }
        )
    }
}

// ── Linha data + hora + botão Agora ──────────────────────────────────────────

@Composable
fun DateTimeNowRow(
    label: String,
    date: String, time: String,
    onDate: (String) -> Unit, onTime: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp, color = SteelLight,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    val d = "%04d-%02d-%02d".format(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    val t = "%02d:%02d".format(
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE)
                    )
                    onDate(d); onTime(t)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Steel),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Agora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            DateField(label = "", value = date, onValueChange = onDate, modifier = Modifier.weight(1.4f))
            TimeField(label = "", value = time, onValueChange = onTime, modifier = Modifier.weight(1f))
        }
    }
}

// ── Bloco de dica ─────────────────────────────────────────────────────────────

@Composable
fun HintBlock(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFBF8F1))
            .border(1.dp, LineGray)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("ℹ️", fontSize = 13.sp)
        Text(text, fontSize = 12.sp, color = SteelLight)
    }
}

// ── Barra de total (ponto) ────────────────────────────────────────────────────

@Composable
fun TotalBar(pausaLabel: String, pausa: String, totalLabel: String, total: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Steel)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(pausaLabel.uppercase(), fontSize = 11.sp, color = Color(0xFFC9D2DA), fontWeight = FontWeight.Bold)
            Text(pausa, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                fontFamily = FontFamily.Monospace)
        }
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(.2f)))
        Column(modifier = Modifier.weight(1f)) {
            Text(totalLabel.uppercase(), fontSize = 11.sp, color = Color(0xFFC9D2DA), fontWeight = FontWeight.Bold)
            Text(total, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White,
                fontFamily = FontFamily.Monospace)
        }
    }
}

// ── Resumo de tempo por dia ───────────────────────────────────────────────────

@Composable
fun TimeSummaryRow(pausa: String, total: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        listOf("Tempo em pausa" to pausa, "Tempo no atendimento" to total).forEach { (lbl, v) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFFBF8F1), RoundedCornerShape(4.dp))
                    .border(1.dp, LineGray, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Text(lbl.uppercase(), fontSize = 10.sp, color = SteelLight, fontWeight = FontWeight.Bold, letterSpacing = .4.sp)
                Text(v, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Steel, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// ── Card de rótulo numerado (empréstimo) ─────────────────────────────────────

@Composable
fun SubSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 13.sp, fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .background(MitsubishiRed, RoundedCornerShape(6.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
fun SubSectionTitle(text: String, modifier: Modifier = Modifier) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = text,
        fontSize = 13.sp, fontWeight = FontWeight.Bold,
        color = Steel,
        modifier = modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(4.dp))
    HorizontalDivider(color = LineGray, thickness = 1.5.dp)
    Spacer(Modifier.height(12.dp))
}
