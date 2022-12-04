<?php
$server_name = $_SERVER['SERVER_NAME'];
include("connect.php");

error_reporting(E_ALL);
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
ini_set('track_errors', 1);

$kode_sekolah = isset($_POST['kode_sekolah']) ? $_POST['kode_sekolah'] : '';
$nip = isset($_POST['nip']) ? $_POST['nip'] : '';
$pwd = isset($_POST['password']) ? $_POST['password'] : '';

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

if ($domain != NULL) {

	$password = md5($pwd);

	$query = "SELECT employee.employee_id, 
	employee_nip, 
	employee_name, 
	employee_password, 
	employee_email, 
	employee_phone, 
	employee_gender, 
	employee_address, 
	employee_born_place, 
	employee_born_date, 
	employee_photo, 
	employee_strata, 
	employee_position_id, 
	employee.employee_majors_id,
	employee_status, 
	employee_start, 
	employee_end, 
	employee_category, 
	employee_input_date,  
	employee.jarak_radius, 
	employee.area_absen,
	employee.status_present, 
	employee_last_update," . $database . "get_status_absen(status_absen_temp,status_absen) validasi,
	'PEGAWAI' AS employee_role_id_name, 
	(SELECT position_name FROM " . $database . "position 
	WHERE position_id=employee.employee_position_id) 
	employee_position_id FROM " . $database . "employee 
	WHERE employee_nip=? AND employee_password=?";

	//Retrieving the contents of the table
	$stmt = mysqli_prepare($koneksi, $query);
	mysqli_stmt_bind_param($stmt, "ss", $nip, $password);
	//Executing the statement
	mysqli_stmt_execute($stmt);
	//Getting the result
	$sql = mysqli_stmt_get_result($stmt);
	//Closing the statement
	mysqli_stmt_close($stmt);

	//$sql = mysqli_query($koneksi, $query);
	$check_user = mysqli_num_rows($sql);

	if ($check_user > 0) {

		$row = mysqli_fetch_assoc($sql);
		$id_employee = $row['employee_id'];
		$radius = $row['jarak_radius'];

		if (isset($row['employee_photo'])) {
			//$photo = "http://".$domain.".adminsekolah.net/uploads/employee/".$row["employee_photo"];
			$photo = "http://$server_name/indoweb/AdminSekolah_Presensi/" . $domain . "/uploads/employee/" . $row["employee_photo"];
		} else {
			//$photo="http://".$domain.".adminsekolah.net/img/avatar_user.png";
			$photo = "http://$server_name/indoweb/AdminSekolah_Presensi/profile.jpeg";
		}

		// ?
		function GetValue_2($tablename, $column, $where)
		{
			global $koneksi;
			$sql = "SELECT $column FROM $tablename WHERE $where";
			$rowult_get_value = mysqli_query($koneksi, $sql);
			$row_get_value = mysqli_fetch_row($rowult_get_value);
			return (isset($row_get_value[0])) ? $row_get_value[0] : '';
		}

		$area = $row['area_absen'];
		$area = explode(",", $area);
		$jumlah = count($area);

		for ($i = 0; $i < $jumlah; $i++) {
			$longitude = GetValue_2($database . "area_absensi", "longi", "id_area = '" . $area[$i] . "'");
			$latitude = GetValue_2($database . "area_absensi", "lati", "id_area = '" . $area[$i] . "'");
			$lokasi = GetValue_2($database . "area_absensi", "nama_area", "id_area = '" . $area[$i] . "'");

			$data_area[] = [
				'lokasi' => $lokasi,
				'longitude' => $longitude,
				'latitude' => $latitude,
			];
		}

		$get_datang_pulang = "SELECT (SELECT IFNULL(MIN(time),'') FROM " . $database . "data_absensi 
		WHERE id_pegawai='$id_employee' 
		AND jenis_absen='DATANG' 
		AND tanggal=STR_TO_DATE(SYSDATE(), '%Y-%m-%d')) datang,
		(SELECT IFNULL(MAX(time),'') FROM " . $database . "data_absensi 
		WHERE id_pegawai='$id_employee' 
		AND jenis_absen='PULANG' 
		AND tanggal=STR_TO_DATE(SYSDATE(), '%Y-%m-%d')) pulang 
		";

		$sql_datang_pulang = mysqli_query($koneksi, $get_datang_pulang);
		$array_datang_pulang  = mysqli_fetch_assoc($sql_datang_pulang);
		$time_datang = substr($array_datang_pulang['datang'], 0, 5);
		$time_pulang = substr($array_datang_pulang['pulang'], 0, 5);

		$set_mode_absen = "";
		if ($row['status_present'] == '1') {
			$set_mode_absen = "DATANG PULANG";
		} else if ($row['status_present'] == '2') {
			$set_mode_absen = "PELAJARAN";
		} else {
			$set_mode_absen = "DUA MODE";
		}

		$email = $row['employee_email'];
		if ($email == NULL) {
			$email = "";
		}

		$json = [
			'is_correct' => true,
			'message' => "success",
			'data' => [
				'username' => "$id_employee",
				'nip' => $row['employee_nip'],
				'nama' => $row['employee_name'],
				'phone' => $row['employee_phone'],
				'email' => $email,
				'jabatan' => $row['employee_position_id'],
				'role_id' => $row['employee_role_id_name'],
				'max_datang' => $time_datang,
				'max_pulang' => $time_pulang,
				'validasi' => $row['validasi'],
				'photo' => $photo,
				'area' => $data_area,
				'jarak_radius' => "$radius",
				'mode_absen' => $set_mode_absen
			]
		];
	} else {
		$json = [
			'is_correct' => false,
			'message' => "NIP atau Password Anda salah."
		];
	}
} else {
	$json = [
		'is_correct' => false,
		'message' => "Kode Sekolah Anda salah."
	];
}

mysqli_info($koneksi);

header("Content-type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT);
