<?php
include("connect.php");

$kode_sekolah = isset($_GET['kode_sekolah']) ? $_GET['kode_sekolah'] : '';
$id_majors = isset($_GET['id_unit']) ? $_GET['id_unit'] : '';

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
    class_id, 
    class.class_name 
    FROM " . $database . "class 
    WHERE majors_majors_id='$id_majors'";

    $sql = mysqli_query($koneksi, $query);
    $num_rows = mysqli_num_rows($sql);
    if ($num_rows != 0) {
        while ($row = mysqli_fetch_assoc($sql)) {
            $class[] = [
                'id_kelas' => $row['class_id'],
                'nama_kelas' => $row['class_name']
            ];
        }

        $json = [
            'is_correct' => true,
            'message' => "success",
            'kelas' => $class
        ];
    } else {
        $json = [
            'is_correct' => false,
            'message' => "Kelas Tidak Ditemukan."
        ];
    }
} else {
    $json = [
        'is_correct' => false,
        'message' => "Kode Sekolah Invalid."
    ];
}

mysqli_close($koneksi);

header("Content-type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT);
