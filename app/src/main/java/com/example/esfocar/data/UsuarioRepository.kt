package com.example.esfocar.data

import androidx.compose.runtime.mutableStateMapOf
import com.example.esfocar.model.Usuario

class UsuarioRepository {
    private val _usuarios = mutableStateMapOf<String, Usuario>()

    init {
        _usuarios["demo@esfocar.cl"] = Usuario("demo@esfocar.cl", "Profesor Demo", "Esfocar123")
    }

    // CREATE
    fun registrar(usuario: Usuario): Boolean {
        val emailKey = usuario.email.trim().lowercase()
        if (_usuarios.containsKey(emailKey)) return false
        _usuarios[emailKey] = usuario
        return true
    }

    // READ
    fun buscarPorEmail(email: String): Usuario? {
        return _usuarios[email.trim().lowercase()]
    }
}