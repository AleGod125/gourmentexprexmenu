package com.example.gourmentexprexmenu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.DnsResolver.Callback
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gourmentexprexmenu.adapters.ArrozacompañamtesAdapter
import com.example.gourmentexprexmenu.adapters.ProteinasAdapter
import com.example.gourmentexprexmenu.adapters.acompañamtesAdapter
import com.example.gourmentexprexmenu.adapters.bebidasAdapter
import com.example.gourmentexprexmenu.databinding.ActivityMainBinding
import com.example.gourmentexprexmenu.dialogs.dialogAñadir
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrar("Proteinas")
        mostrar("Acompañantes")
        mostrar("Arroz")
        mostrar("Bebidas")



        binding.btnCompartir.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val prompt =
                        "Tengo un negocio (Gourmet Express) que consiste en una cocina oculta en la que ofrezco almuerzos a domicilio , por lo general mis clientes son personas en su lugar de trabajo como bancos , callcenters, clínicas , radios etc , quisiera un mensaje amigable de 20 palabras donde le doy los buenos días y los invito a comprar para acompañar el envío de la carta por WhatsApp."
                    val response = OpenAIRetrofitClient.openAIApiService.generateText(
                        "Bearer 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",
                        OpenAIRequest(prompt, 20)
                    )

                    response.enqueue(object : retrofit2.Callback<OpenAIResponse> {
                        override fun onResponse(
                            call: Call<OpenAIResponse>,
                            response: Response<OpenAIResponse>
                        ) {
                            val generatedText = response.body().toString()
                            share(viewToBitmap(binding.menu), this@MainActivity, generatedText)

                        }

                        override fun onFailure(call: Call<OpenAIResponse>, t: Throwable) {
                            Toast.makeText(
                                this@MainActivity,
                                "Error ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    })

                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error al generar el texto: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.popmenu.setOnClickListener { view ->
            val popuMenu = PopupMenu(this, view)

            popuMenu.inflate(R.menu.popup_menu)

            popuMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.protrinas -> {
                        dialogAñadir { text ->
                            add(text, "Proteinas")
                        }.show(supportFragmentManager, "descripcion")

                        true
                    }

                    R.id.Acompañantes -> {
                        dialogAñadir { text ->
                            add(text, "Acompañantes")
                        }.show(supportFragmentManager, "descripcion")
                        true
                    }

                    R.id.Arroz -> {
                        dialogAñadir { text ->
                            add(text, "Arroz")
                        }.show(supportFragmentManager, "descripcion")
                        true
                    }

                    R.id.bebidas -> {
                        dialogAñadir { text ->
                            add(text, "Bebidas")
                        }.show(supportFragmentManager, "descripcion")
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popuMenu.show()
        }

        binding.popmenuEliminar.setOnClickListener { view ->
            val popuMenu = PopupMenu(this, view)

            popuMenu.inflate(R.menu.popup_menu_deleate)

            popuMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.protrinas -> {
                        removeAll("Proteinas")
                        true
                    }

                    R.id.Acompañantes -> {
                        removeAll("Acompañantes")
                        true
                    }

                    R.id.Arroz -> {
                        removeAll("Arroz")
                        true
                    }

                    R.id.bebidas -> {
                        removeAll("Bebidas")
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popuMenu.show()
        }

        binding.precio.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.precio.setBackgroundResource(android.R.color.darker_gray)
            } else {
                binding.precio.setBackgroundResource(android.R.color.transparent)
            }
        }
        binding.root.setOnClickListener {
            binding.precio.clearFocus()
        }


    }


    fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun share(bitmap: Bitmap, context: Context, textList: String) {

        val imagePath = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "title",
            null
        )
        val imageUri = Uri.parse(imagePath)

        if (imageUri != null) {
            val shareMessage = textList

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            Toast.makeText(context, "Error al compartir imagen", Toast.LENGTH_SHORT).show()
        }
    }

    fun mostrar(collection: String) {
        val itemList = mutableListOf<String>()
        db.collection(collection)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val nombre = document.getString("nombre")
                    nombre?.let {
                        itemList.add(it)
                    }
                }
                when (collection) {
                    "Proteinas" -> {
                        setupRecyclerView(binding.recyclerProteinas, ProteinasAdapter(itemList))
                    }

                    "Acompañantes" -> {
                        setupRecyclerView(
                            binding.recyclerAcompaAntes,
                            acompañamtesAdapter(itemList)
                        )
                    }

                    "Arroz" -> {
                        setupRecyclerView(binding.recyclerArroz, ArrozacompañamtesAdapter(itemList))
                    }

                    "Bebidas" -> {
                        setupRecyclerView(binding.recyclerbebidas, bebidasAdapter(itemList))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@MainActivity,
                    "Error al obtener las proteínas: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun add(text: String, collection: String) {
        db.collection(collection).document(text).set(
            hashMapOf(
                "nombre" to text
            )
        ).addOnSuccessListener {
            Toast.makeText(this, "'$text' agregado a $collection", Toast.LENGTH_SHORT).show()
            mostrar(collection)
            notifyDataSetChangedForCollection(collection)
        }.addOnFailureListener { exception ->
            Toast.makeText(
                this,
                "Error al agregar '$text' a $collection: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAll(collection: String) {
        db.collection(collection)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val docId = document.id
                    db.collection(collection)
                        .document(docId)
                        .delete()
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Error al eliminar: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                Toast.makeText(
                    this,
                    "Todos los elementos de $collection han sido eliminados",
                    Toast.LENGTH_SHORT
                ).show()
                mostrar(collection)
                notifyDataSetChangedForCollection(collection)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error al obtener documentos: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyDataSetChangedForCollection(collection: String) {
        when (collection) {
            "Proteinas" -> binding.recyclerProteinas.adapter?.notifyDataSetChanged()
            "Acompañantes" -> binding.recyclerAcompaAntes.adapter?.notifyDataSetChanged()
            "Arroz" -> binding.recyclerArroz.adapter?.notifyDataSetChanged()
            "Bebidas" -> binding.recyclerbebidas.adapter?.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}