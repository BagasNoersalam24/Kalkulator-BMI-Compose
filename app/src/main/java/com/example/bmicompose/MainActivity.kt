package com.example.bmicompose

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
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
    val tampilkanTips by viewModel.tampilkanTips.collectAsState()
    if (tampilkanTips) {
        LayarTips(onKembali = { viewModel.navigasiKeTips(false) })
    } else {
        LayarKalkulator(viewModel)
    }
}

@Composable
fun LayarKalkulator(viewModel: BmiViewModel) {
    val context = LocalContext.current

    // Membaca data kontak yang tersimpan di HP (SharedPreferences)
    val sharedPref = context.getSharedPreferences("KontakPref", Context.MODE_PRIVATE)
    var emailTersimpan by remember { mutableStateOf(sharedPref.getString("EMAIL", "") ?: "") }
    var waTersimpan by remember { mutableStateOf(sharedPref.getString("WA", "") ?: "") }

    // State lokal untuk inputan Kalkulator
    var umur by remember { mutableStateOf("") }
    var berat by remember { mutableStateOf("") }
    var tinggi by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Pria") }

    // State untuk memunculkan Pop-up
    var tampilkanDialogBagikan by remember { mutableStateOf(false) }
    var tampilkanDialogDaftar by remember { mutableStateOf(false) }

    // State form pendaftaran kontak sementara
    var inputEmail by remember { mutableStateOf(emailTersimpan) }
    var inputWa by remember { mutableStateOf(waTersimpan) }

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
        Text("Kalkulator BMI Pro", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(riwayat, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = gender == "Pria", onClick = { gender = "Pria" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1E3A8A)))
            Text("Pria")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = gender == "Wanita", onClick = { gender = "Wanita" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE11D48)))
            Text("Wanita")
        }

        OutlinedTextField(value = umur, onValueChange = { umur = it }, label = { Text("Umur (Tahun)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        OutlinedTextField(value = berat, onValueChange = { berat = it }, label = { Text("Berat Badan (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        OutlinedTextField(value = tinggi, onValueChange = { tinggi = it }, label = { Text("Tinggi Badan (cm)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

        Button(
            onClick = {
                if (umur.isBlank() || berat.isBlank() || tinggi.isBlank()) {
                    Toast.makeText(context, "Mohon isi semua data!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.hitungBmi(umur, berat, tinggi, gender, context)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A), contentColor = Color.White)
        ) {
            Text("Hitung BMI")
        }

        OutlinedButton(
            onClick = { umur = ""; berat = ""; tinggi = ""; gender = "Pria"; viewModel.resetData() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ulangi / Hapus")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AREA HASIL
        if (hasilBmi.isNotEmpty()) {
            Text(hasilBmi, color = Color(warnaHasil), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // TOMBOL BAGIKAN (Satu tombol utama)
            Button(
                onClick = {
                    // Cek apakah pengguna sudah mendaftarkan kontaknya?
                    if (emailTersimpan.isEmpty() || waTersimpan.isEmpty()) {
                        tampilkanDialogDaftar = true // Munculkan pop up daftar kontak
                    } else {
                        tampilkanDialogBagikan = true // Munculkan pop up pilihan WA/Email
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Bagikan")
            }

            Button(
                onClick = { viewModel.navigasiKeTips(true) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Tips Kesehatan")
            }
        }
    }

    // --- POP UP 1: DAFTAR KONTAK (Hanya muncul jika belum pernah daftar) ---
    if (tampilkanDialogDaftar) {
        AlertDialog(
            onDismissRequest = { tampilkanDialogDaftar = false },
            title = { Text("Daftar Kontak Pribadi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Simpan email dan WhatsApp Anda sekali saja untuk menerima laporan kesehatan secara instan.", fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                    OutlinedTextField(value = inputEmail, onValueChange = { inputEmail = it }, label = { Text("Alamat Email Anda") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                    OutlinedTextField(value = inputWa, onValueChange = { inputWa = it }, label = { Text("No WA (Contoh: 0812...)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (inputEmail.isNotBlank() && inputWa.isNotBlank()) {
                        // Simpan permanen ke SharedPreferences
                        sharedPref.edit().putString("EMAIL", inputEmail).putString("WA", inputWa).apply()
                        emailTersimpan = inputEmail
                        waTersimpan = inputWa
                        tampilkanDialogDaftar = false
                        tampilkanDialogBagikan = true // Langsung buka pop-up bagikan
                    } else {
                        Toast.makeText(context, "Mohon isi keduanya!", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Simpan & Lanjutkan") }
            },
            dismissButton = {
                TextButton(onClick = { tampilkanDialogDaftar = false }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    // --- POP UP 2: PILIH PLATFORM (Email atau WhatsApp) ---
    if (tampilkanDialogBagikan) {
        AlertDialog(
            onDismissRequest = { tampilkanDialogBagikan = false },
            title = { Text("Kirim Laporan", fontWeight = FontWeight.Bold) },
            text = { Text("Kirim hasil BMI ini ke kontak pribadi Anda:") },
            confirmButton = {
                // TOMBOL WHATSAPP
                Button(
                    onClick = {
                        var nomorFormat = waTersimpan
                        if (nomorFormat.startsWith("0")) nomorFormat = "62" + nomorFormat.substring(1)

                        val pesanTeks = "Halo,\n\nIni adalah catatan BMI pribadi saya:\n\n$hasilBmi\n\n- Dikirim dari BMI Pro"
                        val pesanTerEncode = URLEncoder.encode(pesanTeks, "UTF-8")

                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$nomorFormat&text=$pesanTerEncode"))
                        try {
                            context.startActivity(waIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp tidak terinstal.", Toast.LENGTH_SHORT).show()
                        }
                        tampilkanDialogBagikan = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) { Text("WhatsApp") }
            },
            dismissButton = {
                // TOMBOL EMAIL
                Button(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            // Perhatikan: Kita langsung menaruh Email pengguna di dalam mailto:
                            data = Uri.parse("mailto:$emailTersimpan")
                            putExtra(Intent.EXTRA_SUBJECT, "Catatan Pribadi: Hasil Cek BMI")
                            putExtra(Intent.EXTRA_TEXT, "Halo,\n\nIni adalah catatan BMI pribadi saya:\n\n$hasilBmi\n\n- Dikirim dari BMI Pro")
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Kirim via Email"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak ada aplikasi Email.", Toast.LENGTH_SHORT).show()
                        }
                        tampilkanDialogBagikan = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Email") }
            }
        )
    }
}

@Composable
fun LayarTips(onKembali: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("💡 Tips Kesehatan", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
        Text("1. Jaga Hidrasi:\nMinum air minimal 2-3 liter per hari.\n\n2. Pola Makan:\nPerbanyak protein dan serat alami, kurangi gula buatan.\n\n3. Olahraga Teratur:\nMinimal 30 menit per hari, 3-5 kali seminggu.\n\n4. Istirahat Cukup:\nTidur 7-8 jam sangat penting.", fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.padding(bottom = 32.dp))
        Button(onClick = onKembali, modifier = Modifier.fillMaxWidth()) { Text("Kembali ke Kalkulator") }
    }
}