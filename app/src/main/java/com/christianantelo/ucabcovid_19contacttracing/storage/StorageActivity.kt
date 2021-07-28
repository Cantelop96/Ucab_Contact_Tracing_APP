package com.christianantelo.ucabcovid_19contacttracing.storage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.christianantelo.ucabcovid_19contacttracing.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


abstract class StorageActivity : AppCompatActivity() {

    // [START storage_field_declaration]
    lateinit var storage: FirebaseStorage
    // [END storage_field_declaration]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage) // TODO: 6/26/21 crear view para almacenar informacion respuestas de formas subida y bajada de archivos

        storage = Firebase.storage

    }
    fun includesForCreateReference() {
        val storage = Firebase.storage

        // ## Crear una referencia para la base de datos

        // [START create_storage_reference]
        // Creamos una referenca a la carpeta principal
        var storageRef = storage.reference
        // Creamos referencia a las diferentes carpetas
        var partiturasRef: StorageReference? = storageRef.child("partituras")
        var formasRef: StorageReference? = storageRef.child("formas")

    }
}