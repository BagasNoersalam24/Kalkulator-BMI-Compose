package com.example.bmicompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    // Menyambungkan ViewModel (Otak) ke Activity
    private val viewModel: BmiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AplikasiBmi(viewModel)
                }
            }
        }
    }
}

@Composable
fun AplikasiBmi(viewModel: BmiViewModel) {
    // Memantau State dari ViewModel
    val tampilkanTips by viewModel.tampilkanTips.collectAsState()

    // Navigasi sederhana ala Compose
    if (tampilkanTips) {
        LayarTips(onKembali = { viewModel.navigasiKeTips(false) })
    } else {
        LayarKalkulator(viewModel)
    }
}

@Composable
fun LayarKalkulator(viewModel: BmiViewModel) {
    val context = LocalContext.current

    // State lokal untuk inputan (karena ini hanya urusan UI saat mengetik)
    var umur by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }
    var tinggi by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Pria") }

    // Memantau hasil dari ViewModel
    val hasilBmi by viewModel.bmiResult.collectAsState()
    val warnaHasil by viewModel.warnaHasil.collectAsState()
    val riwayat by viewModel.riwayat.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kalkulator BMI ", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(riwayat, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

        // Pilihan Gender (Radio Buttons)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = gender == "Pria", onClick = { gender = "Pria" },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF1E3A8A),
                    unselectedColor = Color.Gray
                )
            )
            Text("Pria")

            Spacer(modifier = Modifier.width(16.dp))

            RadioButton(selected = gender == "Wanita", onClick = { gender = "Wanita" },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF1E3A8A),
                    unselectedColor = Color.Gray
                )
            )
            Text("Wanita")
        }

        // Input Data
        OutlinedTextField(
            value = umur, onValueChange = { umur = it },
            label = { Text("Umur (Tahun)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = berat, onValueChange = { berat = it },
            label = { Text("Berat Badan (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = tinggi, onValueChange = { tinggi = it },
            label = { Text("Tinggi Badan (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Tombol Aksi
        Button(
            onClick = { if (umur.isBlank() || berat.isBlank() || tinggi.isBlank()) {

                android.widget.Toast.makeText(
                    context,
                    "Mohon isi Umur, Berat, dan Tinggi Badan terlebih dahulu!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {

                viewModel.hitungBmi(umur, berat, tinggi, gender, context)
            }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A8A),
                    contentColor = Color.White
            )
        ){
            Text("Hitung BMI")
        }

        OutlinedButton(
            onClick = {
                umur = ""; berat = ""; tinggi = ""; gender = "Pria"
                viewModel.resetData()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ulangi / Hapus")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (hasilBmi.isNotEmpty()) {
            Text(hasilBmi, color = Color(warnaHasil), fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {

                        data = android.net.Uri.parse("mailto:")

                        putExtra(Intent.EXTRA_SUBJECT, "Laporan Hasil Cek Kesehatan (BMI)")

                        putExtra(Intent.EXTRA_TEXT, "Halo,\n\nBerikut adalah hasil pengecekan tubuh saya menggunakan Kalkulator BMI Pro:\n\n$hasilBmi\n\nTetap sehat dan semangat!")
                    }

                    try {
                        context.startActivity(Intent.createChooser(emailIntent, "Kirim hasil melalui Email..."))
                    } catch (e: Exception) {

                        android.widget.Toast.makeText(context, "Tidak ada aplikasi Email yang terinstal.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kirim ke Email")
            }

            Button(
                onClick = { viewModel.navigasiKeTips(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Lihat Tips Kesehatan")
            }
        }
    }
}

@Composable
fun LayarTips(onKembali: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💡 Tips Kesehatan", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        Text(
            "1. Jaga Hidrasi:\nMinum air minimal 2-3 liter per hari.\n\n2. Pola Makan:\nPerbanyak protein dan serat alami, kurangi gula buatan.\n\n3. Olahraga Teratur:\nMinimal 30 menit per hari, 3-5 kali seminggu.\n\n4. Istirahat Cukup:\nTidur 7-8 jam sangat penting untuk metabolisme tubuh.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(onClick = onKembali, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Kalkulator")
        }
    }
}