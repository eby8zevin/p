<?php
include_once("fungsi/date.php");
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

$today = pretty_date(date("Y-m-d"),  'l',  FALSE);

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
	AND day.day_name='$today'";

	$sql = mysqli_query($koneksi, $query);
	$check_user = mysqli_num_rows($sql);
	if ($check_user > 0) {

		while ($row = mysqli_fetch_assoc($sql)) {
			$schedule[] = [
				'jadwal_id' => $row['schedule_id'],
				'id_hari' => $row['schedule_day'],
				'hari' => $row['day_name'],
				'kelas' => $row['class_name'],
				'id_nama_pelajaran' => $row['schedule_lesson_id'],
				'nama_pelajaran' => $row['lesson_name'],
				'waktu' => $row['schedule_time'],
				'guru' => $row['employee_name']
			];
		}

		$json = [
			'is_correct' => true,
			'message' => "success",
			'jadwal' => $schedule
		];
	} else {
		$json = [
			'is_correct' => false,
			'message' => "Tidak ada Jadwal untuk Hari ini."
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
