package com.example.esfocar.data

import androidx.compose.runtime.mutableStateListOf
import com.example.esfocar.model.Documento

class DocumentoRepository {
    private val _documentos = mutableStateListOf<Documento>()

    // READ
    fun obtenerDocumentos(): List<Documento> = _documentos

    // CREATE
    fun agregar(documento: Documento): Boolean {
        if (_documentos.none { it.uri == documento.uri }) {
            _documentos.add(documento)
            return true
        }
        return false
    }

    // UPDATE
    fun actualizar(
        id: String,
        alumno: String = "",
        nota: String = "",
        notaGuardada: String = "",
        observacion: String = ""
    ): Boolean {
        val index = _documentos.indexOfFirst { it.id == id }
        if (index != -1) {
            val actual = _documentos[index]
            _documentos[index] = actual.copy(
                alumno = alumno.ifEmpty { actual.alumno },
                nota = nota.ifEmpty { actual.nota },
                notaGuardada = notaGuardada.ifEmpty { actual.notaGuardada },
                observacion = observacion.ifEmpty { actual.observacion }
            )
            return true
        }
        return false
    }

    // DELETE
    fun eliminar(id: String): Boolean {
        return _documentos.removeIf { it.id == id }
    }
}