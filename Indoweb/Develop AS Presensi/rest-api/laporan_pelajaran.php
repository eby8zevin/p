<?php
include("connect.php");
include_once("month_indonesian.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_GET['kode_sekolah']) ? $_GET['kode_sekolah'] : '';
$id_pegawai = isset($_GET['id_pegawai']) ? $_GET['id_pegawai'] : '';
$type = isset($_GET['type']) ? $_GET['type'] : '';
$year = isset($_GET['tahun']) ? $_GET['tahun'] : '';
$month = isset($_GET['bulan']) ? $_GET['bulan'] : '';

$bulan = strtoupper($month);
if ($bulan == "JANUARI") {
    $bulan = "1";
} elseif ($bulan == "FEBRUARI") {
    $bulan = "2";
} elseif ($bulan == "MARET") {
    $bulan = "3";
} elseif ($bulan == "APRIL") {
    $bulan = "4";
} elseif ($bulan == "MEI") {
    $bulan = "5";
} elseif ($bulan == "JUNI") {
    $bulan = "6";
} elseif ($bulan == "JULI") {
    $bulan = "7";
} elseif ($bulan == "AGUSTUS") {
    $bulan = "8";
} elseif ($bulan == "SEPTEMBER") {
    $bulan = "9";
} elseif ($bulan == "OKTOBER") {
    $bulan = "10";
} elseif ($bulan == "NOVEMBER") {
    $bulan = "11";
} elseif ($bulan == "DESEMBER") {
    $bulan = "12";
}

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
        if ($type == "BULANAN" && $year != NULL && $bulan != NULL) {

            $bulan_hari_total = cal_days_in_month(CAL_GREGORIAN, $bulan, $year);

            $year_month = date('Y-m', strtotime($year . "-" . $bulan));

            $sql = mysqli_query($koneksi, "SELECT * FROM " . $database . "data_absensi_pelajaran 
            WHERE id_pegawai='$id_pegawai' 
            AND bulan='$year_month'
            ORDER BY tanggal, time");

            $lesson = array();
            while ($row = mysqli_fetch_assoc($sql)) {

                // $timestamp = strtotime($row['tanggal']);
                // // Creating new date format from that timestamp
                // $new_date = date("d M Y", $timestamp);

                $convert_tanggal = tanggal_indonesia($row['tanggal']);

                $lesson[] = [
                    'tanggal' => $convert_tanggal,
                    'jam_absen' => date_format(new DateTime($row['time']), "H:i"),
                    'status' => ucwords(strtolower($row['jenis_absen'])),
                    'kelas' => $row['name_class'],
                    'nama_pelajaran' => $row['name_lesson'],
                    'jam_pelajaran' => $row['schedule_time'],
                    'lokasi' => $row['lokasi']
                ];
            }

            $get_data = "SELECT 
			(SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='HADIR' 
			AND bulan='$year_month') hadir, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran 
			WHERE id_pegawai='$id_pegawai' 
			AND (jenis_absen='HADIR' 
            OR jenis_absen='IJIN' 
            OR jenis_absen='SAKIT' 
            OR jenis_absen='LAIN-LAIN')
			AND YEAR(tanggal)='$year') hadir_tahun_ini, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='IJIN' 
			AND  bulan='$year_month') ijin,

            (SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='SAKIT' 
			AND  bulan='$year_month') sakit,

            (SELECT COUNT(1) FROM " . $database . "data_absensi_pelajaran 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='LAIN-LAIN' 
			AND  bulan='$year_month') lain
			";

            $sql_get_data = mysqli_query($koneksi, $get_data);
            $array_get_data = mysqli_fetch_assoc($sql_get_data);

            $json = [
                'is_correct' => true,
                'message' => "success",
                'data' => [
                    'hadir' => $array_get_data['hadir'],
                    'ijin' => $array_get_data['ijin'],
                    'sakit' => $array_get_data['sakit'],
                    'lain' => $array_get_data['lain'],
                    'hadir_tahun_ini' =>  $array_get_data['hadir_tahun_ini'],
                    'rekap' => $lesson
                ]
            ];
        } else {
            $json = [
                'is_correct' => false,
                'message' => "Type atau Tahun Bulan Anda salah."
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
