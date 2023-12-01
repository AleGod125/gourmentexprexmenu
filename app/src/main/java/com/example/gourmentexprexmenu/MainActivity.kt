package com.example.gourmentexprexmenu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gourmentexprexmenu.adapters.ArrozacompañamtesAdapter
import com.example.gourmentexprexmenu.adapters.ProteinasAdapter
import com.example.gourmentexprexmenu.adapters.acompañamtesAdapter
import com.example.gourmentexprexmenu.adapters.bebidasAdapter
import com.example.gourmentexprexmenu.databinding.ActivityMainBinding
import com.example.gourmentexprexmenu.dialogs.dialogAñadir
import com.example.gourmentexprexmenu.dialogs.dialogEliminar
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

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
            shareImage(viewToBitmap(binding.menu), this)
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

                        dialogEliminar { text ->
                            remove(text, "Proteinas")
                        }.show(supportFragmentManager, "descripcion")
                        true
                    }

                    R.id.Acompañantes -> {
                        dialogEliminar { text ->
                            remove(text, "Acompañantes")
                        }.show(supportFragmentManager, "descripcion")
                        true
                    }

                    R.id.Arroz -> {
                        dialogEliminar { text ->
                            remove(text, "Arroz")
                        }.show(supportFragmentManager, "descripcion")
                        true
                    }

                    R.id.bebidas -> {
                        dialogEliminar { text ->
                            remove(text, "Bebidas")
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
    }

    fun viewToBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun shareImage(bitmap: Bitmap, context: Context) {
        val imagePath = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "title",
            null
        )
        val imageUri = Uri.parse(imagePath)

        if (imageUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } else {
            Toast.makeText(context, "Error al compartir imagen", Toast.LENGTH_SHORT).show()
        }
    }

    fun mostrar(collection: String){
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
                        setupRecyclerView(binding.recyclerAcompaAntes, acompañamtesAdapter(itemList))
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
                Toast.makeText(this@MainActivity, "Error al obtener las proteínas: ${exception.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Error al agregar '$text' a $collection: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun remove(text: String, collection: String) {
        db.collection(collection)
            .whereEqualTo("nombre", text)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val docId = documents.documents[0].id
                    db.collection(collection)
                        .document(docId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "'$text' eliminado de $collection", Toast.LENGTH_SHORT).show()
                            mostrar(collection)
                            notifyDataSetChangedForCollection(collection)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error al eliminar '$text': ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "No se encontró '$text' en la lista de $collection", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al buscar '$text': ${exception.message}", Toast.LENGTH_SHORT).show()
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