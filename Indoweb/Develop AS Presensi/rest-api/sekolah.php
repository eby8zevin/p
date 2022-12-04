<?php
$server_name = $_SERVER['SERVER_NAME'];
include("connect.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_GET['kode_sekolah']) ? $_GET['kode_sekolah'] : '';

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
$waktuindonesia = GetValue("sekolahs", "waktu_indonesia", "kode_sekolah='$kode_sekolah'");

if ($database == "adminsek_demo.") {
    $db_host = 'localhost';
    $db_user = 'root';
    $db_pass = '';
    $db_name = 'adminsek_demo';
    $koneksi = mysqli_connect($db_host, $db_user, $db_pass, $db_name);
}

//$logo = "http://" . $domain . ".adminsekolah.net/uploads/school/" . GetValue($database . "setting", "setting_value", "setting_id=6");
$logo = "http://" . $server_name . "/indoweb/AdminSekolah_Presensi/" . $domain . "/" . GetValue($database . "setting", "setting_value", "setting_id=6");
//echo $database;

if ($domain != NULL) {
    $json = [
        'is_correct' => true,
        'message' => "success",
        'data' => [
            'kode_sekolah' => $kode_sekolah,
            'nama_sekolah' => $nama_sekolah,
            'alamat_sekolah' => $alamat_sekolah,
            'domain' => $domain,
            'logo' => $logo,
            'waktu_indonesia' => $waktuindonesia
        ]
    ];
} else {
    $json = [
        'is_correct' => false,
        'message' => "Kode Sekolah Anda salah."
    ];
}

mysqli_close($koneksi);

header("Content-type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT);
