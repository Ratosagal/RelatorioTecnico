package com.example.relatriotcnicomitsubishielectric.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.relatriotcnicomitsubishielectric.ui.theme.*

private const val VALID_USER = "mitsubishi"
private const val VALID_PASS = "12345678"

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val senhaFocus = remember { FocusRequester() }

    fun tentar() {
        erro = ""
        if (usuario.isBlank() || senha.isBlank()) {
            erro = "Preencha usuario e senha."
            return
        }
        if (usuario.trim() == VALID_USER && senha == VALID_PASS) {
            loading = true
            onLoginSuccess()
        } else {
            erro = "Usuario ou senha incorretos."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Steel, Color(0xFF1B2730))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo + marca
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.relatriotcnicomitsubishielectric.R.drawable.ic_mitsubishi_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(70.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "MITSUBISHI",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        lineHeight = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "ELECTRIC",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp,
                        lineHeight = 28.sp,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "CNC / Automação Industrial",
                color = Color(0xFFC9D2DA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(36.dp))

            // Card do formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Acesso ao Sistema RAT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Steel
                    )
                    HorizontalDivider(color = LineGray)

                    // Campo usuario
                    Column {
                        Text(
                            "Usuario",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        OutlinedTextField(
                            value = usuario,
                            onValueChange = { usuario = it; erro = "" },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Digite seu usuario", color = SteelLight.copy(.5f)) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { senhaFocus.requestFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MitsubishiRed,
                                unfocusedBorderColor = LineGray,
                                cursorColor = MitsubishiRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }

                    // Campo senha
                    Column {
                        Text(
                            "Senha",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                        OutlinedTextField(
                            value = senha,
                            onValueChange = { senha = it; erro = "" },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(senhaFocus),
                            singleLine = true,
                            placeholder = { Text("Digite sua senha", color = SteelLight.copy(.5f)) },
                            visualTransformation = if (senhaVisivel) VisualTransformation.None
                                                   else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { tentar() }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                                    Icon(
                                        if (senhaVisivel) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = if (senhaVisivel) "Ocultar senha" else "Mostrar senha",
                                        tint = SteelLight
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MitsubishiRed,
                                unfocusedBorderColor = LineGray,
                                cursorColor = MitsubishiRed
                            ),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp)
                        )
                    }

                    // Mensagem de erro
                    AnimatedVisibility(
                        visible = erro.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = erro,
                            color = MitsubishiRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Botao entrar
                    Button(
                        onClick = { tentar() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MitsubishiRed),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Entrar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "Relatorio de Assistencia Tecnica v1.0",
                color = Color(0xFF8A9BA8),
                fontSize = 11.sp
            )
        }
    }
}

