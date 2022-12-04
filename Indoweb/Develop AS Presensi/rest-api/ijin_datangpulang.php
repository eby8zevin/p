<?php
$server_name = $_SERVER['DOCUMENT_ROOT'];
include("connect.php");
include_once("month_indonesian.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_POST['kode_sekolah']) ? $_POST['kode_sekolah'] : null;
$id_pegawai = isset($_POST['id_pegawai']) ? $_POST['id_pegawai'] : null;
$jenis_absen = isset($_POST['jenis']) ? $_POST['jenis'] : null;
$catatan_absen = isset($_POST['keterangan']) ? $_POST['keterangan'] : null;
$tgl_awal = isset($_POST['tgl_awal']) ? date('Y-m-d', strtotime($_POST['tgl_awal'])) : null;
$tgl_akhir = isset($_POST['tgl_akhir']) ? date('Y-m-d', strtotime($_POST['tgl_akhir'])) : null;

// ?
function GetValue($tablename, $column, $where)
{
    global $koneksi;
    $sql = "SELECT $column FROM $tablename WHERE $where";
    $rowult_get_value = mysqli_query($koneksi, $sql);
    $row_get_value = mysqli_fetch_row($rowult_get_value);
    return (isset($row_get_value[0])) ? $row_get_value[0] : null;
}

$database = GetValue("sekolahs", "db", "kode_sekolah='$kode_sekolah'") . ".";
$folder = GetValue("sekolahs", "folder", "kode_sekolah='$kode_sekolah'");
$waktuindonesia = GetValue("sekolahs", "waktu_indonesia", "kode_sekolah='$kode_sekolah'");

if ($waktuindonesia == "WIB") {
    date_default_timezone_set('Asia/Jakarta');
} elseif ($waktuindonesia == "WITA") {
    date_default_timezone_set('Asia/Makassar');
} elseif ($waktuindonesia == "WIT") {
    date_default_timezone_set('Asia/Jayapura');
}

if ($database == "adminsek_demo.") {
    $db_host = 'root';
    $db_user = '';
    $db_pass = '';
    $db_name = 'adminsek_demo';
    $koneksi = mysqli_connect($db_host, $db_user, $db_pass, $db_name);
}

$check_type = array("IJIN", "SAKIT", "LAIN-LAIN");
$created_date  = date("Y-m-d H:i:s");
$time = date("H:i:s");

// Path to move uploaded files
//$target_path = "http://domain.com/uploads/";
$target_path = "izin/";

// final file url that is being uploaded
$file_upload_url = $target_path;

// getting server ip address
$server_ip = gethostbyname(gethostname());

if ($folder != NULL) {

    list($ijin, $sakit, $lain) = $check_type;
    if ($jenis_absen == $ijin || $jenis_absen == $sakit || $jenis_absen == $lain) {

        $check_user = mysqli_query($koneksi, "SELECT * FROM " . $database . "employee 
        WHERE employee_id='$id_pegawai'");
        $num_check_user = mysqli_num_rows($check_user);

        if ($num_check_user > 0) {

            $begin = new DateTime($tgl_awal);
            $end = new DateTime($tgl_akhir);
            $end = $end->modify('+1 day');
            $interval = DateInterval::createFromDateString('1 day');
            $period = new DatePeriod($begin, $interval, $end);

            if ($tgl_awal == $tgl_akhir || $tgl_awal < $tgl_akhir) {

                foreach ($period as $dt) {
                    $date = $dt->format("Y-m-d");

                    // $convert = strtotime($date);
                    // // Creating new date format from that timestamp
                    // $new_date = date("d-M-Y", $convert);
                    $new_date = tanggal_indonesia($date);

                    $check_izin = mysqli_query($koneksi, "SELECT * FROM " . $database . "data_absensi 
                    WHERE id_pegawai='$id_pegawai' 
                    AND tanggal='$date'
                    ");

                    $get_jenis_absen = mysqli_fetch_assoc($check_izin);
                    $result_jenis_absen = isset($get_jenis_absen['jenis_absen']) ? $get_jenis_absen['jenis_absen'] : 0;
                    $num_check_izin = mysqli_num_rows($check_izin);

                    if ($num_check_izin == 0) {

                        $nama_pegawai = GetValue($database . "employee", "employee_name", "employee_id ='$id_pegawai'");
                        $lokasi = GetValue($database . "area_absensi", "nama_area", "id_area ='1'");

                        $date_ex = explode("-", $date);
                        $bulan = date('Y-m', strtotime($date_ex[0] . "-" . $date_ex[1]));

                        $query_insert = "INSERT INTO " . $database . "data_absensi SET 
                        id_pegawai = '$id_pegawai',
                        area_absen = '1',
                        bulan = '$bulan',
                        tanggal = '$date',
                        time = '$time',
                        jenis_absen = '$jenis_absen',
                        catatan_absen = '$catatan_absen',
                        lokasi = '$lokasi',
                        created_by = '$nama_pegawai',
                        created_date = '$created_date'
                        ";

                        $sql_insert = mysqli_query($koneksi, $query_insert);

                        if ($sql_insert) {
                            $json = [
                                'is_correct' => true,
                                'message' => "Presensi $jenis_absen Berhasil Dikirim !"
                            ];
                        } else {
                            $json = [
                                'is_correct' => false,
                                'message' => "Gagal! " . mysqli_error($koneksi)
                            ];
                        }
                    } else {
                        $json = [
                            'is_correct' => false,
                            'message' => "Gagal! Anda sudah melakukan Presensi $result_jenis_absen pada tanggal " . $new_date
                        ];
                    }
                }
            } else {
                $json = [
                    'is_correct' => false,
                    'message' => "Gagal! Tanggal Awal harus lebih kecil dari Tanggal Akhir."
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
            'message' => "Jenis Absen Anda salah."
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
