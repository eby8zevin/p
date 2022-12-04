<?php
include("connect.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_GET["kode_sekolah"]) ? $_GET['kode_sekolah'] : '';
$id_pegawai = isset($_GET["id_pegawai"]) ? $_GET['id_pegawai'] : '';
$type = isset($_GET["type"]) ? $_GET['type'] : '';
$tahun = isset($_GET["tahun"]) ? $_GET['tahun'] : '';

function GetValue($tablename, $column, $where)
{
	global $koneksi;
	$sql = "SELECT $column FROM $tablename WHERE $where";
	$rowult_get_value = mysqli_query($koneksi, $sql);
	$row_get_value = mysqli_fetch_row($rowult_get_value);
	return $row_get_value[0];
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

		if ($type == "TAHUNAN" && $tahun != NULL) {

			$query_data = "SELECT
			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='DATANG' 
			AND YEAR(tanggal)='$tahun') hadir, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='IJIN' 
			AND YEAR(tanggal)='$tahun') ijin, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='SAKIT' 
			AND YEAR(tanggal)='$tahun') sakit,

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='LAIN-LAIN' 
			AND YEAR(tanggal)='$tahun') lain
			";

			$sql_data = mysqli_query($koneksi, $query_data);
			$result_data = mysqli_fetch_assoc($sql_data);

			$total_hadir_lain = $result_data['hadir'] + $result_data['ijin'] + $result_data['sakit'] + $result_data['lain'];
			$percentase = $total_hadir_lain * 100 / 264;
			$percentase_hari = $total_hadir_lain . "/264 Hari";

			$sql_terlambat = mysqli_query($koneksi, "SELECT COUNT(1) terlambat 
			FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND YEAR(tanggal)='$tahun' 
			AND jenis_absen='DATANG' 
			AND statushadir='Terlambat'");
			$result_terlambat = mysqli_fetch_assoc($sql_terlambat);

			$json =  [
				'ís_correct' => true,
				'message' => "success",
				'hadir' => $result_data['hadir'],
				'ijin' => $result_data['ijin'],
				'sakit' => $result_data['sakit'],
				'lain' => $result_data['lain'],
				'terlambat' => $result_terlambat['terlambat'],
				'percentase' => number_format($percentase, 2),
				'percentase_hari' => $percentase_hari
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
