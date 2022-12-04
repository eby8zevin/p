<?php
include("connect.php");

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
$database = GetValue("sekolahs", "db", "kode_sekolah='$kode_sekolah'") . ".";
$domain = GetValue("sekolahs", "folder", "kode_sekolah='$kode_sekolah'");
$waktuindonesia = GetValue("sekolahs", "waktu_indonesia", "kode_sekolah='$kode_sekolah'");

if ($domain != NULL) {

    $query = "SELECT 
    majors_id,
    majors_short_name 
    FROM " . $database . "majors";

    $sql = mysqli_query($koneksi, $query);
    while ($row = mysqli_fetch_assoc($sql)) {
        $unit[] = [
            'id_unit' => $row['majors_id'],
            'nama_unit' => $row['majors_short_name']
        ];
    }

    $json = [
        'is_correct' => true,
        'message' => "success",
        'unit' => $unit
    ];
} else {
    $json = [
        'is_correct' => false,
        'message' => "Kode Sekolah Invalid."
    ];
}

mysqli_close($koneksi);

header("Content-type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT);
