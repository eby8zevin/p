<?php
$server_name = $_SERVER['DOCUMENT_ROOT'];
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
$image = isset($_POST['image']) ? $_POST['image'] : '';

$id_majors = isset($_POST['id_unit']) ? $_POST['id_unit'] : '';
$id_class = isset($_POST['id_kelas']) ? $_POST['id_kelas'] : '';
$id_schedule = isset($_POST['id_jadwal']) ? $_POST['id_jadwal'] : '';
$id_lesson = isset($_POST['id_pelajaran']) ? $_POST['id_pelajaran'] : '';
$time_lesson = isset($_POST['jam_pelajaran']) ? $_POST['jam_pelajaran'] : '';

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

$created_date = date("Y-m-d H:i:s");
$date = date("Y-m-d");
$time = date("H:i:s");
$year_month = date("Y-m");

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

if ($folder != NULL) {

    if ($type == 'PELAJARAN' && $lokasi != NULL && $id_class != NULL && $id_lesson != NULL) {

        $check_user = mysqli_query($koneksi, "SELECT * FROM " . $database . "employee WHERE employee_id='$id_pegawai'");
        $get_name = mysqli_fetch_assoc($check_user);
        $name_employee = $get_name['employee_name'];
        $num_check_user = mysqli_num_rows($check_user);

        if ($num_check_user > 0) {

            $name_class = GetValue($database . "class", "class_name", "class_id = '" . $id_class . "'");
            $name_lesson = GetValue($database . "lesson", "lesson_name", "lesson_id = '" . $id_lesson . "'");
            $id_area =  GetValue($database . "area_absensi", "id_area", "nama_area = '" . $lokasi . "'");

            $explode = explode(" - ", $time_lesson);
            $first = $explode[0];
            $end = $explode[1];

            $checkTime = date("H:i");

            if ($checkTime >= $first && $checkTime <= $end) {

                $query_get_data = "SELECT * FROM " . $database . "data_absensi_pelajaran 
                WHERE id_pegawai='$id_pegawai' 
                AND tanggal='$date' 
                AND id_lesson='$id_lesson'
                AND (jenis_absen='HADIR'
                OR jenis_absen='IJIN'
                OR jenis_absen='SAKIT'
                OR jenis_absen='LAIN-LAIN')";

                $sql_get_data = mysqli_query($koneksi, $query_get_data);
                $result_get_data = mysqli_num_rows($sql_get_data);

                if ($result_get_data == 0) {

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

                    $query_insert = "INSERT INTO " . $database . "data_absensi_pelajaran SET
                    id_pegawai = '$id_pegawai',
                    jenis_absen = 'HADIR',
                    area_absen = '$id_area',
                    bulan = '$year_month',
                    tanggal = '$date',
                    time = '$time',
                    lokasi = '$lokasi',
                    longi = '$longi',
                    lati = '$lati',
                    foto = '$foto',
                    remark = '$remark',
                    created_by = '$name_employee',
                    created_date = '$created_date',
                    id_majors = '$id_majors',
                    id_class = '$id_class', 
                    name_class = '$name_class',
                    id_schedule = '$id_schedule',
                    id_lesson = '$id_lesson', 
                    name_lesson = '$name_lesson',
                    schedule_time = '$time_lesson'
                    ";

                    $sql_insert = mysqli_query($koneksi, $query_insert);

                    if ($sql_insert) {
                        $json = [
                            'is_correct' => true,
                            'message' => "Absen Anda Berhasil Dikirim !"
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
                        'message' => "Absen Gagal. Anda Sudah Melakukan Presensi !"
                    ];
                }
            } else {
                $json = [
                    'is_correct' => false,
                    'message' => "Absen Gagal. Anda Diluar Jam Pelajaran !"
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
            'message' => "Type, Lokasi, Kelas, Pelajaran Anda salah."
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
