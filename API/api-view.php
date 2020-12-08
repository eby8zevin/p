<?php
include 'koneksi.php';

//syntax melihat semua record data yang ada di tabel data_mhs
$sql = "SELECT * FROM data_mhs";

//eksekusi query diatas
$query = sqlsrv_query($conn, $sql);
while($data=sqlsrv_fetch_array($query)){
  $item[] = array (
    'ID_datamhs' => $data['ID_datamhs'],
    'NIM' => $data['NIM'],
    'Nama' => $data['Nama'],
    'Prodi' => $data['Prodi'],
    'TanggalBulanTahun' => $data['TanggalBulanTahun']
  );
}

//menampung data yang dihasilkan
$json = array (
  'result' => 'Succes',
  'item' => $item
 );
 
//merubah data kedalam bentuk JSON
echo json_encode($json);


//pdo
// $response = [];

// $query = $conn->prepare("SELECT * FROM data_mhs ORDER BY NIM ASC");
// $query->execute();

// if ($query->rowCount() == 0) {
//     $response['status'] = false;
//     $response['message'] = "Data tidak ditemukan";
// } else {
//     while ($data = $query->fetch()) {
//         $response[]= [
//           'ID_datamhs' => $data['ID_datamhs'],
//           'NIM' => $data['NIM'],
//           'Nama' => $data['Nama'],
//           'Program_Studi' => $data['Prodi'],
//           'Tanggal_Bulan_Tahun' => $data['TanggalBulanTahun']
//        ];
//     }
// }

// $json = json_encode($response, JSON_PRETTY_PRINT);
// echo $json;
?>
