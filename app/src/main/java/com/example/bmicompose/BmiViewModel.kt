package com.example.bmicompose

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BmiViewModel : ViewModel() {

    // STATE: Data yang akan dipantau oleh layar (UI)
    private val _bmiResult = MutableStateFlow("")
    val bmiResult: StateFlow<String> = _bmiResult.asStateFlow()

    private val _warnaHasil = MutableStateFlow(0xFF808080)
    val warnaHasil: StateFlow<Long> = _warnaHasil.asStateFlow()

    private val _riwayat = MutableStateFlow("Belum ada riwayat")
    val riwayat: StateFlow<String> = _riwayat.asStateFlow()

    private val _tampilkanTips = MutableStateFlow(false)
    val tampilkanTips: StateFlow<Boolean> = _tampilkanTips.asStateFlow()

    // LOGIKA: Fungsi untuk menghitung BMI
    fun hitungBmi(umur: String, berat: String, tinggiCm: String, gender: String, context: Context) {
        if (umur.isEmpty() || berat.isEmpty() || tinggiCm.isEmpty()) return

        val b = berat.toDoubleOrNull() ?: 0.0
        val t = (tinggiCm.toDoubleOrNull() ?: 0.0) / 100

        if (b > 0 && t > 0) {
            val bmi = b / (t * t)
            tentukanStatus(bmi, umur, gender)
        }
    }

    private fun tentukanStatus(bmi: Double, umur: String, gender: String) {
        val (status, warna) = when {
            bmi < 18.5 -> "Kekurangan Berat Badan 🥣" to 0xFF2196F3 // Biru
            bmi < 24.9 -> "Normal 💪😊" to 0xFF4CAF50 // Hijau
            bmi < 29.9 -> "Kelebihan Berat Badan 🏃‍♂️" to 0xFFFF9800 // Oranye
            else -> "Obesitas ⚠️" to 0xFFF44336 // Merah
        }

        val hasilFormat = String.format("%.2f", bmi)
        val teksAkhir = "Saya $gender ($umur thn).\nBMI: $hasilFormat ($status)"

        // Memperbarui State
        _bmiResult.value = teksAkhir
        _warnaHasil.value = warna
        _riwayat.value = "Terakhir: $hasilFormat ($status)"
    }

    fun resetData() {
        _bmiResult.value = ""
        _warnaHasil.value = 0xFF808080
        _tampilkanTips.value = false
    }

    fun navigasiKeTips(buka: Boolean) {
        _tampilkanTips.value = buka
    }
}
