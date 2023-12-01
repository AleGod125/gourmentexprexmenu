package com.example.gourmentexprexmenu.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.gourmentexprexmenu.databinding.DialogAgregarElementoBinding
import kotlin.reflect.KFunction1


class dialogAñadir(private val listener: (String) -> Unit) : DialogFragment() {

   private lateinit var binding: DialogAgregarElementoBinding



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogAgregarElementoBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        binding.agregar.setOnClickListener {
            val textoIngresado = binding.editText.text.toString()
            listener.invoke(textoIngresado)
            dismiss()
        }

        binding.cancelar.setOnClickListener {
            dismiss()
        }



        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog

    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window ?: return
        val params = window.attributes
        params.width = (getScreenWidth() * 0.90).toInt()
        window.attributes = params
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

}