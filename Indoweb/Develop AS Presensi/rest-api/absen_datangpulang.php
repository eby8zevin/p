<?php
$server_name = $_SERVER['DOCUMENT_ROOT'];
include_once("fungsi/date.php");
include("connect.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_POST['kode_sekolah']) ? $_POST['kode_sekolah'] : '';
$id_pegawai = isset($_POST['id_pegawai']) ? $_POST['id_pegawai'] : '';
$type = isset($_POST['type']) ? $_POST['type'] : '';
$lokasi = isset($_POST['lokasi']) ? $_POST['lokasi'] : '';
$longi = isset($_POST['longi']) ? $_POST['longi'] : '';
$lati = isset($_POST['lati']) ? $_POST['lati'] : '';
$keterangan = isset($_POST['keterangan']) ? $_POST['keterangan'] : '';
$image = isset($_POST['image']) ? $_POST['image'] : '';

// ?
function GetValue($tablename, $column, $where)
{
    global $koneksi;
    $sql = "SELECT $column FROM $tablename WHERE $where";
    $result_get_value = mysqli_query($koneksi, $sql);
    $row_get_value = mysqli_fetch_row($result_get_value);
    return (isset($row_get_value[0])) ? $row_get_value[0] : '';
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
    $db_host = 'localhost';
    $db_user = 'root';
    $db_pass = '';
    $db_name = 'adminsek_demo';
    $koneksi = mysqli_connect($db_host, $db_user, $db_pass, $db_name);
}

// Path to move uploaded files
//$target_path = "http://domain.com/uploads/";
$target_path = "absensi/";

// final file url that is being uploaded
$file_upload_url = $target_path;

// getting server ip address
$server_ip = gethostbyname(gethostname());

$created_date = date("Y-m-d H:i:s");
$date = date("Y-m-d");
$time = date("H:i:s");
$year_month = date("Y-m");
$year = date("Y");
$month = date("m");

if ($folder != NULL) {

    if ($type == "DATANG" || $type == "PULANG" && $lokasi != NULL) {

        $check_user = mysqli_query($koneksi, "SELECT * FROM " . $database . "employee WHERE employee_id='$id_pegawai'");
        $num_check_user = mysqli_num_rows($check_user);

        if ($num_check_user > 0) {

            $check_date = mysqli_query($koneksi, "SELECT * FROM " . $database . "data_absensi 
            WHERE id_pegawai='$id_pegawai'
            AND tanggal='$date'
            AND (jenis_absen='$type'
            OR jenis_absen='IJIN'
            OR jenis_absen='SAKIT'
            OR jenis_absen='LAIN-LAIN')");

            $get_jenis_absen = mysqli_fetch_array($check_date);
            $jenis_absen = isset($get_jenis_absen['jenis_absen']) ? $get_jenis_absen['jenis_absen'] : 0;

            $num_check_date = mysqli_num_rows($check_date);
            if ($num_check_date == 0) {

                $nama = GetValue($database . "employee", "employee_name", "employee_id ='$id_pegawai'");
                $id_area =  GetValue($database . "area_absensi", "id_area", "nama_area ='$lokasi'");
                $majors_id = GetValue($database . "employee", "employee_majors_id", "employee_id='$id_pegawai'");

                // today
                $date_pretty = pretty_date(date("Y-m-d"),  'l',  FALSE);

                $get_data_waktu = "SELECT data_waktu_masuk, data_waktu_pulang
                FROM " . $database . "data_waktu 
                JOIN " . $database . "day ON data_waktu.data_waktu_day_id = day.day_id 
                JOIN " . $database . "majors ON data_waktu.data_waktu_majors_id = majors.majors_id
                WHERE day_name = '$date_pretty' AND majors_id = '$majors_id'";

                $sql_data_waktu = mysqli_query($koneksi, $get_data_waktu);
                while ($row = mysqli_fetch_assoc($sql_data_waktu)) {
                    $masuk = $row['data_waktu_masuk'];
                    $pulang = $row['data_waktu_pulang'];
                }

                $time_masuk = strtotime("now");
                $jam_hadir = strtotime($masuk);

                $max_masuk = $masuk;
                $min_pulang = $pulang;

                $diff  = $time_masuk - $jam_hadir;
                $jam   = floor($diff / (60 * 60));
                $menit = $diff - $jam * (60 * 60);

                // absen Datang
                if ($type == 'DATANG') {
                    if ($time <= $max_masuk) {
                        $status_hadir = 'Tepat Waktu';
                        $terlambat = 'Tepat Waktu';
                    } else {
                        $status_hadir = 'Terlambat';
                        $terlambat = 'Anda terlambat: ' . $jam .  ' jam lebih ' . floor($menit / 60) . ' menit';
                    }
                }

                // absen Pulang
                if ($type == 'PULANG') {
                    $terlambat = NULL;
                    if ($time < $min_pulang) {
                        $status_hadir =  'Pulang Awal';
                    } else {
                        $status_hadir =  'Pulang';
                    }
                }

                if (isset($_FILES['image']['name'])) {
                    $target_path = $target_path . $id_pegawai . "_" . basename($_FILES['image']['name']);

                    try {
                        // File successfully uploaded
                        // Throws exception incase file is not being moved
                        $foto = $file_upload_url . $id_pegawai . "_" . basename($_FILES['image']['name']);
                        $remark = "SUKSES_UPLOAD";
                        $move_upload = $server_name . "/indoweb/adminsekolah_presensi/$folder/uploads/absensi/" . $id_pegawai . "_" . basename($_FILES['image']['name']);
                        if (!move_uploaded_file($_FILES['image']['tmp_name'], $move_upload)) {
                            // make error flag true
                            $foto = "../uploads/absensi/no_image.jpg";
                            $remark = "GAGAL_UPLOAD1";
                        }
                    } catch (Exception $e) {
                        // Exception occurred. Make error flag true
                        $foto = "../uploads/absensi/no_image.jpg";
                        $remark = "GAGAL_UPLOAD2";
                        $e->getMessage();
                    }
                } else {
                    $foto = "../uploads/absensi/no_image.jpg";
                    $remark = "IMAGE_NOT_FOUND";
                }

                $query_insert =  "INSERT INTO " . $database . "data_absensi SET 
                            id_pegawai = '$id_pegawai', 
                            jenis_absen = '$type', 
                            area_absen = '$id_area', 
                            statushadir = '$status_hadir', 
                            bulan = '$year_month', 
                            tanggal = '$date', 
                            time = '$time',
                            longi = '$longi', 
                            lati = '$lati', 
                            lokasi = '$lokasi', 
                            foto = '$foto',
                            catatan_absen = '$keterangan', 
                            remark = '$remark', 
                            created_by = '$nama',
                            created_date = '$created_date',
                            keterlambatan = '$terlambat'";

                $sql_insert = mysqli_query($koneksi, $query_insert);

                if ($sql_insert) {
                    $json = [
                        'is_correct' => true,
                        'message' => "Absen " . $type . " Anda Berhasil Dikirim !"
                    ];
                } else {
                    $json = [
                        'is_correct' => false,
                        'message' => "Absen Gagal! " . mysqli_error($koneksi)
                    ];
                }
            } else {
                $json = [
                    'is_correct' => false,
                    'message' => "Absen Gagal. Anda Sudah Melakukan Presensi $jenis_absen !"
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
            'message' => "Type atau Lokasi Anda salah."
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
