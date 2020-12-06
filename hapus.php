<?php
 // panggil file koneksi
 include 'koneksi.php';
 
 //ambil data id dari parameter
 $ID=$_GET['ID'];

 $sql="DELETE FROM data_mhs WHERE ID='$ID'";
 $query=sqlsrv_query($conn,$sql) or die(sqlsrv_errors());
 if ($query) {
  //redirect ke halaman index
  echo "<script>alert('Data berhasil dihapus!');history.go(-1);</script>";
  echo "<meta http-equiv='refresh' content='0;url=index.php?datadihapus=sukses'>";
 }
 
?>
