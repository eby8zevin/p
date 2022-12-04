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
$tahun = isset($_GET['tahun']) ? $_GET['tahun'] : '';
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
	$check_user = mysqli_num_rows($get_employee);

	if ($check_user > 0) {
		if ($type == 'BULANAN' && $tahun != NULL && $month != NULL) {

			$bulan_hari_total = cal_days_in_month(CAL_GREGORIAN, $bulan, $tahun);

			$year_month = date('Y-m', strtotime($tahun . "-" . $bulan));

			$query_data_monthly = "SELECT 
			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='DATANG' 
			AND bulan='$year_month') hadir, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND (jenis_absen='DATANG' 
			OR jenis_absen='IJIN' 
			OR jenis_absen='SAKIT' 
			OR jenis_absen='LAIN-LAIN')
			AND YEAR(tanggal)='$tahun') hadir_tahun_ini, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='IJIN'  
			AND bulan='$year_month') ijin, 

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='SAKIT'  
			AND bulan='$year_month') sakit,

			(SELECT COUNT(1) FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND jenis_absen='LAIN-LAIN'  
			AND bulan='$year_month') lain
			";

			$sql_data_monthly = mysqli_query($koneksi, $query_data_monthly);
			$row = mysqli_fetch_assoc($sql_data_monthly);

			$total_hadir_lain = $row['hadir'] + $row['ijin'] + $row['sakit'] + $row['lain'];
			$percentase = $total_hadir_lain * 100 / 22;
			$percentase_hari = $total_hadir_lain . "/22 Hari";

			$query_terlambat = "SELECT COUNT(1) terlambat 
			FROM " . $database . "data_absensi 
			WHERE id_pegawai='$id_pegawai' 
			AND bulan='$year_month' 
			AND jenis_absen='DATANG' 
			AND statushadir='Terlambat'";

			$terlambat = mysqli_query($koneksi, $query_terlambat);
			$terlambat = mysqli_fetch_assoc($terlambat);
			$terlambat = $terlambat['terlambat'];

			$get_data_absensi = "SELECT 
			a.id_pegawai, 
			a.tanggal, 
			CASE WHEN a.jenis_absen='DATANG' THEN 'Hadir'
			WHEN a.jenis_absen='IJIN' THEN 'Ijin'
			WHEN a.jenis_absen='SAKIT' THEN 'Sakit'
			WHEN a.jenis_absen='LAIN-LAIN' THEN 'Lain-lain' ELSE '' 
			END jenis_absen, 
			a.lokasi, 
			a.catatan_absen, 
			(SELECT lokasi FROM " . $database . "data_absensi
			WHERE id_pegawai=a.id_pegawai
			AND tanggal=a.tanggal
			AND jenis_absen='PULANG') lokasi_pulang,
			(SELECT catatan_absen FROM " . $database . "data_absensi
			WHERE id_pegawai=a.id_pegawai
			AND tanggal=a.tanggal
			AND jenis_absen='PULANG') catatan_pulang,
			(SELECT IFNULL(MIN(time),'') FROM " . $database . "data_absensi 
			WHERE id_pegawai=a.id_pegawai 
			AND tanggal=a.tanggal 
			AND jenis_absen!='PULANG') jam_datang, 
			(SELECT IFNULL(MAX(time),'') FROM " . $database . "data_absensi 
			WHERE id_pegawai=a.id_pegawai 
			AND tanggal=a.tanggal
			AND jenis_absen='PULANG') jam_pulang
			FROM " . $database . "data_absensi a WHERE bulan = '$year_month' 
			AND a.id_pegawai = '$id_pegawai' 
			AND a.jenis_absen != 'PULANG' 
			ORDER BY tanggal";

			//detail
			$detail = array();

			$sql_data_absensi = mysqli_query($koneksi, $get_data_absensi);
			while ($data = mysqli_fetch_assoc($sql_data_absensi)) {

				$lokasi_pulang = $data['lokasi_pulang'];
				if ($lokasi_pulang == NULL) {
					$lokasi_pulang = "";
				}

				$catatan_pulang = $data['catatan_pulang'];
				if ($catatan_pulang == NULL) {
					$catatan_pulang = "";
				}

				$convert_tanggal = tanggal_indonesia($data['tanggal']);

				$detail[] = array(
					'hari' => $convert_tanggal,
					'status' => $data['jenis_absen'],
					'detail' => array(
						'jam_datang' => $data['jam_datang'],
						'jam_pulang' => $data['jam_pulang'],
						'lokasi_datang' => $data['lokasi'],
						'lokasi_pulang' => $lokasi_pulang,
						'catatan_datang' => $data['catatan_absen'],
						'catatan_pulang' => $catatan_pulang
					)
				);
			}

			$json = [
				'is_correct' => true,
				'message' => "success",
				'hadir' => $row['hadir'],
				'ijin' => $row['ijin'],
				'sakit' => $row['sakit'],
				'lain' => $row['lain'],
				'terlambat' => $terlambat,
				'percentase' => number_format($percentase, 2),
				'percentase_hari' => $percentase_hari,
				'hadir_tahun_ini' => $row['hadir_tahun_ini'],
				'rekap' => $detail,
			];
		} else {
			$json = [
				'is_correct' => false,
				'message' => 'Type atau Tahun Bulan Anda salah.'
			];
		}
	} else {
		$json = [
			'is_correct' => false,
			'message' => 'Anda tidak terdaftar.'
		];
	}
} else {
	$json = [
		'is_correct' => false,
		'message' => 'Kode Sekolah Anda salah.'
	];
}

mysqli_close($koneksi);

header("Content-type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT);
