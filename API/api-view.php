<?php
include 'koneksi.php';

//syntax melihat semua record data yang ada di tabel data_mhs
$sql = "SELECT * FROM data_mhs";

//eksekusi query diatas
$query = sqlsrv_query($sql);
while($data=sqlsrv_fetch_array($query)){
  $item[] = array (
    'ID_datamhs' => $data['ID_datamhs'],
    'Nama' => $data['Nama'],
    'Program Studi' => $data['Prodi'],
    'Tanggal Bulan Tahun' => $data['TanggalBulanTahun']
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
