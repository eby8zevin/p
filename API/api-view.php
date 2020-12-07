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
    'Program_Studi' => $data['Prodi'],
    'Tanggal_Bulan_Tahun' => $data['TanggalBulanTahun']
  );
}

//menampung data yang dihasilkan
$json = array (
  'result' => 'Succes',
  'item' => $item
 );
 
//merubah data kedalam bentuk JSON
echo json_encode($json);
?>
