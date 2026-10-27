package com.example.esfocar.ui.theme

import android.net.Uri
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.esfocar.data.DocumentoRepository
import com.example.esfocar.data.UsuarioRepository
import com.example.esfocar.model.Documento
import com.example.esfocar.model.Usuario
import java.math.BigDecimal

class EsfocarViewModel(
    private val documentoRepo: DocumentoRepository = DocumentoRepository(),
    private val usuarioRepo: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    var pantalla by mutableStateOf("login")
    var nombre by mutableStateOf("")
    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var confirmarClave by mutableStateOf("")
    var profesor by mutableStateOf("")
    var mensaje by mutableStateOf("")

    val documentos: List<Documento> get() = documentoRepo.obtenerDocumentos()

    // --- ACCIONES CRUD USUARIOS ---
    fun iniciarSesion() {
        val email = correo.trim().lowercase()
        val usuario = usuarioRepo.buscarPorEmail(email)

        when {
            correo.isBlank() || clave.isBlank() -> mensaje = "Completa todos los campos."
            usuario?.clave == clave -> {
                profesor = usuario.nombre
                mensaje = ""
                pantalla = "inicio"
            }
            else -> mensaje = "Datos incorrectos."
        }
    }

    fun registrarProfesor() {
        val email = correo.trim().lowercase()
        when {
            nombre.isBlank() -> mensaje = "Ingresa tu nombre."
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> mensaje = "Correo electrónico inválido."
            clave.length < 8 -> mensaje = "Usa al menos 8 caracteres."
            clave != confirmarClave -> mensaje = "Las contraseñas no coinciden."
            usuarioRepo.buscarPorEmail(email) != null -> mensaje = "El correo ya existe."
            else -> {
                val nuevoUsuario = Usuario(email = email, nombre = nombre.trim(), clave = clave)
                if (usuarioRepo.registrar(nuevoUsuario)) {
                    profesor = nuevoUsuario.nombre
                    nombre = ""
                    correo = ""
                    clave = ""
                    confirmarClave = ""
                    mensaje = "Profesor registrado."
                    pantalla = "inicio"
                }
            }
        }
    }

    fun cerrarSesion() {
        profesor = ""
        correo = ""
        clave = ""
        mensaje = "Sesión cerrada."
        pantalla = "login"
    }

    // --- ACCIONES CRUD DOCUMENTOS ---
    fun cargarDocumento(uri: Uri, nombreArchivo: String) {
        val exito = documentoRepo.agregar(Documento(uri = uri, nombre = nombreArchivo))
        mensaje = if (exito) "Documento cargado." else "El documento ya está en la lista."
    }

    fun actualizarCamposDocumento(id: String, alumno: String, nota: String) {
        documentoRepo.actualizar(id = id, alumno = alumno, nota = nota)
    }

    fun actualizarObservacion(id: String, observacion: String) {
        documentoRepo.actualizar(id = id, observacion = observacion)
    }

    fun guardarNota(doc: Documento) {
        val alumno = doc.alumno.trim()
        val entrada = doc.nota.trim().replace(',', '.')
        val notaValida = entrada.matches(Regex("""\d+(\.\d{1,2})?"""))
        val notaBD = if (notaValida) entrada.toBigDecimalOrNull() else null

        when {
            alumno.isBlank() -> mensaje = "Ingresa el nombre del alumno."
            notaBD == null -> mensaje = "Ingresa una nota con máximo 2 decimales."
            notaBD < BigDecimal("1.00") || notaBD > BigDecimal("7.00") -> {
                mensaje = "La nota debe estar entre 1.00 y 7.00."
            }
            else -> {
                val notaTexto = notaBD.setScale(2).toPlainString()
                documentoRepo.actualizar(id = doc.id, notaGuardada = notaTexto)
                mensaje = "Nota $notaTexto registrada para $alumno."
            }
        }
    }

    fun eliminarDocumento(id: String) {
        if (documentoRepo.eliminar(id)) {
            mensaje = "Documento quitado de la lista."
        }
    }
}