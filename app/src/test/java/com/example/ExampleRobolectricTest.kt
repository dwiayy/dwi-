package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Priority
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainTab
import com.example.viewmodel.RemindViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var viewModel: RemindViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        // Bersihkan SharedPreferences sebelum setiap pengujian untuk menjamin isolasi tes
        val sharedPrefs = context.getSharedPreferences("remind_flow_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        
        viewModel = RemindViewModel(context as Application)
    }

    @Test
    fun test01_readAppNameFromContext() {
        val appName = context.getString(R.string.app_name)
        assertEquals("RemindFlow", appName)
    }

    // ==========================================
    // FITUR A: AUTENTIKASI (LOGIN & DAFTAR) - BLACK-BOX TESTING (Minimal 5 Skenario)
    // ==========================================

    @Test
    fun test_auth_01_registrasi_berhasil_dengan_data_valid() {
        // Input: Email baru belum terdaftar, password valid, nama lengkap
        val email = "budi@remindflow.com"
        val password = "rahasiaBudi123"
        val name = "Budi Hartono"

        // Action: Daftarkan akun
        val hasilRegistrasi = viewModel.register(email, password, name)

        // Output Expected: Registrasi mengembalikan nilai "true" (berhasil)
        assertTrue("Skenario 1 (Valid): Registrasi akun baru harus berhasil", hasilRegistrasi)

        // Verifikasi ke penyimpanan local SharedPreferences
        val sharedPrefs = context.getSharedPreferences("remind_flow_prefs", Context.MODE_PRIVATE)
        assertEquals("Sandi tidak tersimpan dengan benar", password, sharedPrefs.getString("pass_budi@remindflow.com", null))
    }

    @Test
    fun test_auth_02_registrasi_gagal_jika_email_sudah_pernah_terdaftar() {
        // Pre-condition: Daftarkan akun pertama
        viewModel.register("budi@remindflow.com", "rahasiaBudi123", "Budi Hartono")

        // Input: Mencoba mendaftarkan ulang dengan email yang sama tapi nama/password berbeda
        val hasilRegistrasiUlang = viewModel.register("budi@remindflow.com", "sandiBaru999", "Budi Dua")

        // Output Expected: Registrasi mengembalikan nilai "false" karena email sudah terpakai
        assertFalse("Skenario 2 (Invalid): Registrasi harus ditolak jika email duplikat", hasilRegistrasiUlang)
    }

    @Test
    fun test_auth_03_login_berhasil_dengan_email_dan_sandi_terdaftar() {
        // Pre-condition: Daftarkan akun budi
        viewModel.register("budi@remindflow.com", "rahasiaBudi123", "Budi Hartono")

        // Input: Memasukkan email dan sandi yang cocok & benar sesuai database
        val hasilLogin = viewModel.loginWithCredentials("budi@remindflow.com", "rahasiaBudi123")

        // Output Expected: Login sukses (true), state aplikasi berubah ke Halaman Utama (MAIN)
        assertTrue("Skenario 3 (Valid): Login harus sukses dengan kredensial benar", hasilLogin)
        assertEquals("Layar harus berpindah ke Halaman Utama (MAIN)", AppScreen.MAIN, viewModel.currentScreen.value)
        assertEquals("User saat ini harus terapdet ke email budi", "budi@remindflow.com", viewModel.currentUserEmail.value)
    }

    @Test
    fun test_auth_04_login_gagal_jika_email_belum_terdaftar_meskipun_sandi_benar_format() {
        // Pre-condition: Daftarkan akun budi
        viewModel.register("budi@remindflow.com", "rahasiaBudi123", "Budi Hartono")

        // Input: Email salah (belum terdaftar di DB) tapi sandi bernilai benar sama seperti akun budi
        val emailSalah = "salah_email@remindflow.com"
        val sandiBenarFormat = "rahasiaBudi123"

        // Action: Coba login
        val hasilLogin = viewModel.loginWithCredentials(emailSalah, sandiBenarFormat)

        // Output Expected: Login harus gagal (false) karena email salah / tidak terdaftar
        assertFalse("Skenario 4 (Invalid): Login harus gagal jika email salah/belum terdaftar", hasilLogin)
        assertNotEquals("Layar tidak boleh masuk ke Halaman Utama (MAIN)", AppScreen.MAIN, viewModel.currentScreen.value)
    }

    @Test
    fun test_auth_05_login_gagal_jika_sandi_salah_meskipun_email_benar_terdaftar() {
        // Pre-condition: Daftarkan akun budi
        viewModel.register("budi@remindflow.com", "rahasiaBudi123", "Budi Hartono")

        // Input: Email benar & terdaftar, tapi password salah ("sandi_salah_123")
        val hasilLogin = viewModel.loginWithCredentials("budi@remindflow.com", "sandi_salah_123")

        // Output Expected: Login gagal (false) karena password salah
        assertFalse("Skenario 5 (Invalid): Login harus gagal jika kata sandi salah", hasilLogin)
        assertEquals("Layar harus tetap berada pada halaman LOGIN", AppScreen.LOGIN, viewModel.currentScreen.value)
    }

    @Test
    fun test_auth_06_sesi_tersimpan_otomatis_untuk_autologin_setelah_login_berhasil() {
        // Pre-condition: Register dan Login sukses
        viewModel.register("budi@remindflow.com", "rahasiaBudi123", "Budi Hartono")
        viewModel.loginWithCredentials("budi@remindflow.com", "rahasiaBudi123")

        // Action: Re-Instansiasi / Recreate ViewModel (Menandakan seolah-olah aplikasi ditutup lalu dibuka kembali)
        val viewModelBaru = RemindViewModel(context as Application)

        // Output Expected: Status login tersimpan di SharedPreferences, sehingga langsung otomatis masuk tanpa mengetik kredensial lagi
        assertEquals("Sesi otomatis login harus aktif", AppScreen.MAIN, viewModelBaru.currentScreen.value)
        assertEquals("Email login tersimpan harus dipulihkan", "budi@remindflow.com", viewModelBaru.currentUserEmail.value)
    }

    @Test
    fun test_auth_07_logout_berhasil_menghapus_sesi_aktif() {
        // Pre-condition: Login aktif
        viewModel.register("budi@remindflow.com", "rahasiaBudi123", "Budi Hartono")
        viewModel.loginWithCredentials("budi@remindflow.com", "rahasiaBudi123")

        // Action: Klik Keluar (Logout)
        viewModel.logout()

        // Output Expected: State layar kembali ke LOGIN, data email dihapus, dan SharedPreferences dibersihkan dari sesi aktif
        assertEquals("Layar harus kembali ke halaman LOGIN", AppScreen.LOGIN, viewModel.currentScreen.value)
        assertEquals("Data pengguna aktif harus kosong", "", viewModel.currentUserEmail.value)

        val sharedPrefs = context.getSharedPreferences("remind_flow_prefs", Context.MODE_PRIVATE)
        assertFalse("Sesi login SharedPreferences harus bernilai FALSE", sharedPrefs.getBoolean("is_logged_in", true))
    }

    // ==========================================
    // FITUR B: MANAJEMEN TUGAS - BLACK-BOX TESTING (Minimal 5 Skenario)
    // ==========================================

    @Test
    fun test_task_01_tambah_tugas_baru_tersimpan_di_daftar() {
        val totalAwal = viewModel.taskList.value.size

        // Input: Form input tugas diisi secara valid
        viewModel.addTask("Latihan UAT Presentasi", "Mempersiapkan demo APK ke dosen penguji", "Minggu depan", false)

        // Output Expected: Total tugas bertambah 1, judul dan deskripsi tugas cocok di sistem
        val totalAkhir = viewModel.taskList.value.size
        assertEquals("Skenario 1: Jumlah tugas harus bertambah 1", totalAwal + 1, totalAkhir)

        val tugasTerbaru = viewModel.taskList.value.last()
        assertEquals("Judul tugas tidak sesuai", "Latihan UAT Presentasi", tugasTerbaru.title)
    }

    @Test
    fun test_task_02_prioritas_otomatis_tinggi_jika_deadline_hari_ini() {
        // Input: Membuat tugas baru dengan isi tanggal deadline mengandung kata kunci "hari ini"
        viewModel.addTask("Revisi Cepat UAT", "Memperbaiki pengujian blackbox", "Sore hari ini jam 5", false)

        val tugasTerbaru = viewModel.taskList.value.last()

        // Output Expected: Sistem pintar melacak kata kunci "hari ini" lalu mengatur Prioritas ke TINGGI secara otomatis
        assertEquals("Skenario 2: Prioritas tugas harus otomatis TINGGI untuk deadline hari ini", Priority.TINGGI, tugasTerbaru.priority)
    }

    @Test
    fun test_task_03_berhasil_mengubah_status_penyelesaian_tugas() {
        viewModel.addTask("Tugas Checklist", "Belajar materi testing", "Lusa", false)
        val tugas = viewModel.taskList.value.last()

        assertFalse("Tugas baru harus default bernilai belum selesai (isCompleted = false)", tugas.isCompleted)

        // Action: Pengguna menekan tombol checklist
        viewModel.toggleTaskCompletion(tugas.id)

        // Output Expected: Status tugas berubah menjadi selesai (true)
        val tugasTerupdate = viewModel.taskList.value.find { it.id == tugas.id }
        assertTrue("Skenario 3: Status penyelesaian tugas harus berubah menjadi TRUE", tugasTerupdate?.isCompleted ?: false)
    }

    @Test
    fun test_task_04_berhasil_memperbarui_detail_seluruh_informasi_tugas() {
        viewModel.addTask("Desain Logo", "Desain awal", "Bulan depan", false)
        val tugasId = viewModel.taskList.value.last().id

        // Action: Melakukan update detail tugas
        viewModel.updateTask(tugasId, "Desain Logo Premium", "Update visual menggunakan Figma", Priority.TINGGI, "Besok", true)

        // Output Expected: Seluruh teks judul, deskripsi, tanggal, dan tingkat prioritas terupdate dengan benar
        val tugasTeredit = viewModel.taskList.value.find { it.id == tugasId }
        assertEquals("Judul gagal diperbarui", "Desain Logo Premium", tugasTeredit?.title)
        assertEquals("Deskripsi gagal diperbarui", "Update visual menggunakan Figma", tugasTeredit?.description)
        assertEquals("Prioritas gagal diperbarui", Priority.TINGGI, tugasTeredit?.priority)
    }

    @Test
    fun test_task_05_hapus_tugas_berhasil_menghilangkan_data_dari_sistem() {
        viewModel.addTask("Tugas Sementara", "Akan langsung dihapus", "Nanti", false)
        val tugas = viewModel.taskList.value.last()
        val jumlahSebelumHapus = viewModel.taskList.value.size

        // Action: Pengguna menekan tombol Hapus/Delete
        viewModel.deleteTask(tugas.id)

        // Output Expected: Tugas hilang dari daftar, jumlah total tugas berkurang kembali
        val jumlahSetelahHapus = viewModel.taskList.value.size
        assertEquals("Skenario 5: Jumlah tugas harus berkurang 1", jumlahSebelumHapus - 1, jumlahSetelahHapus)
        assertNull("Tugas terhapus seharusnya tidak bisa dicari lagi di sistem", viewModel.taskList.value.find { it.id == tugas.id })
    }

    @Test
    fun test_task_06_notifikasi_sistem_terbaca_sesuai_aktivitas() {
        val jumlahNotifikasiAwal = viewModel.notifications.value.size

        // Action: Menambahkan tugas urgent
        viewModel.addTask("Lapor Hasil Tes", "Blackbox testing 10 skenario selesai", "Hari ini sebelum malam", true)

        // Output Expected: Notifikasi bertambah, berisi pesan penting khusus tugas urgent tersebut
        val jumlahNotifikasiAkhir = viewModel.notifications.value.size
        assertTrue("Notifikasi baru harus bertambah ke sistem", jumlahNotifikasiAkhir > jumlahNotifikasiAwal)
        
        val notifPentingTercatat = viewModel.notifications.value.any { 
            it.message.contains("Lapor Hasil Tes") && it.message.contains("PENTING") 
        }
        assertTrue("Pesannya harus memuat kata PENTING karena prioritas tinggi", notifPentingTercatat)
    }

    @Test
    fun test11_addAndToggleRoutineCompletion() {
        val routineSize = viewModel.routineList.value.size
        viewModel.addRoutine("Minum Air 2L", "07:00")
        
        assertEquals("Rutinitas harus bertambah 1", routineSize + 1, viewModel.routineList.value.size)
        val addedRoutine = viewModel.routineList.value.last()
        assertEquals("Nama ke-rutinitas salah", "Minum Air 2L", addedRoutine.title)
        assertFalse("Rutinitas baru harus belum selesai", addedRoutine.isCompleted)

        // Toggle routine status
        viewModel.toggleRoutineCompletion(addedRoutine.id)
        val toggledRoutine = viewModel.routineList.value.find { it.id == addedRoutine.id }
        assertTrue("Rutinitas harus selesai setelah di-toggle", toggledRoutine?.isCompleted ?: false)

        // Delete routine
        viewModel.deleteRoutine(addedRoutine.id)
        assertEquals("Rutinitas harus kembali kosong/tetap semula", routineSize, viewModel.routineList.value.size)
    }

    @Test
    fun test12_timerStateControl() {
        assertEquals("Waktu mulai timer harus 1500 detik (25 menit)", 25 * 60, viewModel.timeLeftSeconds.value)
        assertFalse("Timer tidak boleh berjalan secara default", viewModel.isTimerRunning.value)

        // Start timer
        viewModel.startTimer()
        assertTrue("Timer harus aktif/berjalan", viewModel.isTimerRunning.value)

        // Pause timer
        viewModel.pauseTimer()
        assertFalse("Timer harus berhenti setelah di-pause", viewModel.isTimerRunning.value)

        // Reset timer
        viewModel.resetTimer()
        assertEquals("Waktu timer harus kembali ke 1500 detik", 25 * 60, viewModel.timeLeftSeconds.value)
    }

    // ==========================================
    // FITUR C: MANAJEMEN PROFIL - BLACK-BOX TESTING
    // ==========================================

    @Test
    fun test_profile_01_update_nama_berhasil() {
        // Pre-condition: Login user
        viewModel.register("profil@test.com", "rahasia123", "Nama Lama")
        viewModel.loginWithCredentials("profil@test.com", "rahasia123")

        // Action: Update profile name
        val updateSuccess = viewModel.updateProfileName("Nama Baru Keren")
        assertTrue("Update nama profil harus mengembalikan TRUE", updateSuccess)

        // Verify state
        assertEquals("Nama profil baru tidak terupdate", "Nama Baru Keren", viewModel.getCurrentUserName())
    }

    @Test
    fun test_profile_02_update_sandi_berhasil_jika_sandi_lama_cocok() {
        // Pre-condition: Login user
        viewModel.register("profil@test.com", "rahasia123", "Nama User")
        viewModel.loginWithCredentials("profil@test.com", "rahasia123")

        // Action: Ganti kata sandi
        val changeSuccess = viewModel.updateProfilePassword("rahasia123", "sandiBaru789")
        assertTrue("Ganti kata sandi harus sukses jika kata sandi lama benar", changeSuccess)

        // Log out & Log back in with the new password
        viewModel.logout()
        val reLoginSuccess = viewModel.loginWithCredentials("profil@test.com", "sandiBaru789")
        assertTrue("Harus bisa login kembali menggunakan kata sandi baru", reLoginSuccess)
    }

    @Test
    fun test_profile_03_update_sandi_gagal_jika_sandi_lama_salah() {
        // Pre-condition: Login user
        viewModel.register("profil@test.com", "rahasia123", "Nama User")
        viewModel.loginWithCredentials("profil@test.com", "rahasia123")

        // Action: Ganti sandi dengan memberikan sandi lama salah ("salahSandi")
        val changeSuccess = viewModel.updateProfilePassword("salahSandi", "sandiBaru789")
        assertFalse("Ganti kata sandi harus ditolak jika kata sandi lama salah", changeSuccess)
    }

    @Test
    fun test_profile_04_update_email_berhasil() {
        // Pre-condition: Login user
        viewModel.register("profil@test.com", "rahasia123", "Nama User")
        viewModel.loginWithCredentials("profil@test.com", "rahasia123")

        // Action: Update profile email
        val updateEmailSuccess = viewModel.updateProfileEmail("baru@test.com")
        assertTrue("Update email profil harus mengembalikan TRUE", updateEmailSuccess)

        // Verify state
        assertEquals("Email profil baru tidak terupdate", "baru@test.com", viewModel.currentUserEmail.value)
    }
}

