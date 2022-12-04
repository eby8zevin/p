<?php
include("connect.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_GET['kode_sekolah']) ? $_GET['kode_sekolah'] : '';
$id_pegawai = isset($_GET['id_pegawai']) ? $_GET['id_pegawai'] : '';
$type = isset($_GET['type']) ? $_GET['type'] : '';
$year = isset($_GET['tahun']) ? $_GET['tahun'] : '';

// ?
function GetValue($tablename, $column, $where)
{
    global $koneksi;
    $sql = "SELECT $column FROM $tablename WHERE $where";
    $rowult_get_value = mysqli_query($koneksi, $sql);
    $row_get_value = mysqli_fetch_row($rowult_get_value);
    return (isset($row_get_value[0])) ? $row_get_value[0] : '';
}

$nama_sekolah = GetValue("sekolahs", "nama_sekolah", "kode_sekolah='$kode_sekolah'");
$alamat_sekolah = GetValue("sekolahs", "alamat_sekolah", "kode_sekolah='$kode_sekolah'");
$database = GetValue("sekolahs", "db", "kode_sekolah='$kode_sekolah'") . ".";
$domain = GetValue("sekolahs", "folder", "kode_sekolah='$kode_sekolah'");

if ($database == "adminsek_demo.") {
    $db_host = 'localhost';
    $db_user = 'root';
    $db_pass = '';
    $db_name = 'adminsek_demo';
    $koneksi = mysqli_connect($db_host, $db_user, $db_pass, $db_name);
}

if ($domain != NULL) {

    $get_employee = mysqli_query($koneksi, "SELECT * FROM " . $database . "employee WHERE employee_id='$id_pegawai'");
    $check_employee = mysqli_num_rows($get_employee);

    if ($check_employee > 0) {
        if ($type == "TAHUNAN" && $year != NULL) {

            for ($i = 1; $i <= 12; $i++) {

                $text_month = $i;

                if ($text_month == "1") {
                    $text_month = "Januari";
                } elseif ($text_month == "2") {
                    $text_month = "Februari";
                } elseif ($text_month == "3") {
                    $text_month = "Maret";
                } elseif ($text_month == "4") {
                    $text_month = "April";
                } elseif ($text_month == "5") {
                    $text_month = "Mei";
                } elseif ($text_month == "6") {
                    $text_month = "Juni";
                } elseif ($text_month == "7") {
                    $text_month = "Juli";
                } elseif ($text_month == "8") {
                    $text_month = "Agustus";
                } elseif ($text_month == "9") {
                    $text_month = "September";
                } elseif ($text_month == "10") {
                    $text_month = "Oktober";
                } elseif ($text_month == "11") {
                    $text_month = "November";
                } elseif ($text_month == "12") {
                    $text_month = "Desember";
                }

                $year_month = date('Y-m', strtotime($year . "-" . $i));

                $get_data = "SELECT 
                (SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran
                WHERE id_pegawai='$id_pegawai' 
                AND jenis_absen='HADIR' 
                AND bulan='$year_month') hadir,

                (SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran
                WHERE id_pegawai='$id_pegawai' 
                AND jenis_absen='IJIN' 
                AND bulan='$year_month') ijin,

                (SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran
                WHERE id_pegawai='$id_pegawai' 
                AND jenis_absen='SAKIT' 
                AND bulan='$year_month') sakit,

                (SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran
                WHERE id_pegawai='$id_pegawai' 
                AND jenis_absen='LAIN-LAIN' 
                AND bulan='$year_month') lain
                ";

                $sql_get_data = mysqli_query($koneksi, $get_data);
                $array_get_data = mysqli_fetch_assoc($sql_get_data);

                $bulan[] = [
                    'bulan' => $text_month,
                    'hadir' => $array_get_data['hadir'],
                    'ijin' => $array_get_data['ijin'],
                    'sakit' => $array_get_data['sakit'],
                    'lain' => $array_get_data['lain'],
                ];
            }

            $json = [
                'is_correct' => true,
                'message' => "success",
                'data' => $bulan
            ];
        } else {
            $json = [
                'is_correct' => false,
                'message' => "Type atau Tahun Anda salah."
            ];
        }
    } else {
        $json = [
            'is_correct' => false,
            'message' => "Anda tidak terdaftar."
        ];
    }
} else {
    $json = [
        'is_correct' => false,
        'message' => "Kode Sekolah Anda salah."
    ];
}

mysqli_close($koneksi);

header("Content-type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT);
