package com.example.esfocar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esfocar.ui.theme.EsfocarViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EsfocarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EsfocarApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun EsfocarApp(vm: EsfocarViewModel) {
    val context = LocalContext.current

    val selectorDocumentos = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val nombreArchivo = context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            } ?: "Documento"

            vm.cargarDocumento(uri, nombreArchivo)
        }
    }

    Scaffold(
        topBar = {
            if (vm.pantalla != "login" && vm.pantalla != "registro") {
                Column(Modifier.padding(16.dp)) {
                    Text("ESFOCAR", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Gestión docente")
                }
            }
        },
        bottomBar = {
            if (vm.pantalla != "login" && vm.pantalla != "registro") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { vm.pantalla = "inicio" }) { Text("Inicio") }
                    TextButton(onClick = { vm.pantalla = "documentos" }) { Text("Documentos") }
                    TextButton(onClick = { vm.pantalla = "calificar" }) { Text("Calificar") }
                    TextButton(onClick = { vm.pantalla = "perfil" }) { Text("Perfil") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (vm.mensaje.isNotBlank()) {
                Text(text = vm.mensaje, color = MaterialTheme.colorScheme.primary)
            }

            when (vm.pantalla) {
                "login" -> {
                    Spacer(Modifier.height(30.dp))
                    Text("Bienvenido a ESFOCAR", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Plataforma para profesores")

                    Campo(vm.correo, { vm.correo = it }, "Correo electrónico")
                    Campo(vm.clave, { vm.clave = it }, "Contraseña", oculto = true)

                    Button(onClick = { vm.iniciarSesion() }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                        Text("Iniciar sesión")
                    }
                    OutlinedButton(onClick = { vm.mensaje = ""; vm.pantalla = "registro" }, modifier = Modifier.fillMaxWidth()) {
                        Text("Registrar profesor")
                    }
                    Text("Usuario de prueba:\ndemo@esfocar.cl\nContraseña: Esfocar123")
                }

                "registro" -> {
                    Text("Registrar profesor", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Campo(vm.nombre, { vm.nombre = it }, "Nombre completo")
                    Campo(vm.correo, { vm.correo = it }, "Correo electrónico")
                    Campo(vm.clave, { vm.clave = it }, "Contraseña (mínimo 8 caracteres)", oculto = true)
                    Campo(vm.confirmarClave, { vm.confirmarClave = it }, "Confirmar contraseña", oculto = true)

                    Button(onClick = { vm.registrarProfesor() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Crear cuenta")
                    }
                    TextButton(onClick = { vm.mensaje = ""; vm.pantalla = "login" }) { Text("Volver") }
                }

                "inicio" -> {
                    Text("Panel del profesor", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text("Bienvenido/a, ${vm.profesor}")
                    Boton("Gestionar documentos") { vm.pantalla = "documentos" }
                    Boton("Registrar calificaciones") { vm.pantalla = "calificar" }
                    Text("Documentos cargados: ${vm.documentos.size}")
                }

                "documentos" -> {
                    Text("Documentos de evaluación", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            selectorDocumentos.launch(
                                arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cargar documento")
                    }

                    OutlinedButton(
                        onClick = { vm.pantalla = "visualizar_cargas" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Visualizar cargas")
                    }

                    if (vm.documentos.isEmpty()) Text("No hay documentos cargados.")

                    vm.documentos.forEach { doc ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(doc.nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(doc.uri, context.contentResolver.getType(doc.uri) ?: "*/*")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            vm.mensaje = "No se encontró una aplicación para abrir el archivo."
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Abrir documento") }

                                OutlinedButton(onClick = { vm.pantalla = "calificar" }, modifier = Modifier.fillMaxWidth()) { Text("Poner nota") }
                                OutlinedButton(onClick = { vm.eliminarDocumento(doc.id) }, modifier = Modifier.fillMaxWidth()) { Text("Eliminar de la lista") }
                            }
                        }
                    }
                }

                "calificar" -> {
                    Text("Calificar alumnos", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    if (vm.documentos.isEmpty()) Text("No hay documentos cargados.")

                    vm.documentos.forEach { doc ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(doc.nombre, fontWeight = FontWeight.Bold)

                                Campo(doc.alumno, { vm.actualizarCamposDocumento(doc.id, it, doc.nota) }, "Nombre del alumno")
                                Campo(doc.nota, { vm.actualizarCamposDocumento(doc.id, doc.alumno, it) }, "Nota (ej. 6.75)")

                                OutlinedTextField(
                                    value = doc.observacion,
                                    onValueChange = { vm.actualizarObservacion(doc.id, it) },
                                    label = { Text("Observación de la evaluación") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    maxLines = 4
                                )

                                Button(onClick = { vm.guardarNota(doc) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar nota") }

                                if (doc.notaGuardada.isNotBlank()) {
                                    Text("Nota guardada: ${doc.notaGuardada}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "visualizar_cargas" -> {
                    Text(
                        "Visualizar cargas",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Aquí puedes consultar los documentos cargados y sus calificaciones.")

                    if (vm.documentos.isEmpty()) {
                        Text("No hay documentos cargados.")
                    } else {
                        vm.documentos.forEach { doc ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        doc.nombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )

                                    if (doc.notaGuardada.isNotBlank()) {
                                        Text("Estado: Evaluado")
                                        Text("Alumno: ${doc.alumno}")
                                        Text(
                                            "Calificación: ${doc.notaGuardada}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Observación: ${
                                                doc.observacion.ifBlank { "Sin observaciones" }
                                            }"
                                        )
                                    } else {
                                        Text("Estado: Pendiente de calificación")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            vm.pantalla = "calificar"
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Ir a calificar")
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { vm.pantalla = "documentos" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a documentos")
                    }
                }

                "perfil" -> {
                    Text("Perfil del profesor", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Nombre: ${vm.profesor}")
                    Button(onClick = { vm.cerrarSesion() }, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
                }
            }
        }
    }
}

@Composable
fun Campo(valor: String, cambio: (String) -> Unit, etiqueta: String, oculto: Boolean = false) {
    OutlinedTextField(
        value = valor,
        onValueChange = cambio,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (oculto) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun Boton(texto: String, accion: () -> Unit) {
    Button(onClick = accion, modifier = Modifier.fillMaxWidth().height(54.dp)) {
        Text(texto, fontSize = 17.sp)
    }
}