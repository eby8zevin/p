<?php
include("connect.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_GET['kode_sekolah']) ? $_GET['kode_sekolah'] : '';
$id_pegawai = isset($_GET['id_pegawai']) ? $_GET['id_pegawai'] : '';
$id_class = isset($_GET['id_kelas']) ? $_GET['id_kelas'] : '';

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

if ($domain != NULL) {

    $query = "SELECT 
	schedule.schedule_id, 
	schedule.schedule_day,
	day.day_name, 
	class.class_name, 
	schedule.schedule_lesson_id,
	lesson.lesson_name, 
	schedule.schedule_time, 
	employee.employee_name 
	FROM " . $database . "schedule 
	JOIN " . $database . "day ON schedule.schedule_day = day.day_id
	JOIN " . $database . "class ON schedule.schedule_class_id = class.class_id 
	JOIN " . $database . "lesson ON schedule.schedule_lesson_id= lesson.lesson_id 
	JOIN " . $database . "employee ON lesson.lesson_teacher = employee.employee_id 
	WHERE lesson_teacher = '$id_pegawai' 
	AND class_id='$id_class' 
	";

    $sql = mysqli_query($koneksi, $query);
    $check_user = mysqli_num_rows($sql);

    $senin = [];
    $sabtu = [];
    $minggu = [];
    if ($check_user > 0) {

        while ($row = mysqli_fetch_assoc($sql)) {

            $day_name = $row['day_name'];

            if ($day_name == "Senin") {
                $senin[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            } else if ($day_name == "Selasa") {
                $selasa[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            } else if ($day_name == "Rabu") {
                $rabu[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            } else if ($day_name == "Kamis") {
                $kamis[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            } else if ($day_name == "Jumat") {
                $jumat[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            } else if ($day_name == "Sabtu") {
                $sabtu[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            } else if ($day_name == "Minggu") {
                $minggu[] = [
                    'nama_pelajaran' => $row['lesson_name'],
                    'waktu' => $row['schedule_time']
                ];
            }
        }

        if ($sabtu == NULL) {
            $sabtu = [];
        }

        $day[] = [
            'senin' => $senin,
            'selasa' => $selasa,
            'rabu' => $rabu,
            'kamis' => $kamis,
            'jumat' => $jumat,
            'sabtu' => $sabtu,
            'minggu' => $minggu
        ];

        // $day[] = [
        //     'senin' => $senin == NULL ? [] : $senin,
        //     'selasa' => $selasa == NULL ? [] : $selasa,
        //     'rabu' => $rabu == NULL ? [] : $rabu,
        //     'kamis' => $kamis == NULL ? [] : $kamis,
        //     'jumat' => $jumat == NULL ? [] : $jumat,
        //     'sabtu' => $sabtu == NULL ? [] : $sabtu,
        //     'minggu' => $minggu == NULL ? [] : $minggu
        // ];

        $json = [
            'is_correct' => true,
            'message' => "success",
            'hari' => $day
        ];
    } else {
        $json = [
            'is_correct' => false,
            'message' => "Tidak ada Jadwal untuk Kelas ini."
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
