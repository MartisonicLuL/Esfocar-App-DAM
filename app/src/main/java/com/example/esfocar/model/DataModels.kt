package com.example.esfocar.model

import android.net.Uri
import java.util.UUID

data class Documento(
    val id: String = UUID.randomUUID().toString(),
    val uri: Uri,
    val nombre: String,
    val alumno: String = "",
    val nota: String = "",
    val notaGuardada: String = "",
    val observacion: String = ""
)

data class Usuario(
    val email: String,
    val nombre: String,
    val clave: String
)

data class Criterio(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val indicador: String = "",
    val puntaje: String = "10",
    val porcentaje: String = "0"
)

data class Rubrica(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val criterios: List<Criterio> = emptyList()
)