<?php
include 'koneksi.php';
?>

<!DOCTYPE html>
<html lang="en">
<head>
 <title>Edit Data</title>
 <meta charset="UTF-8">
 <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
 <div align="center">
  <h3>Simpan Data SQL Server dengan PHP<br>Form Edit Data</h3>
  
  <?php
   //ambil data id dari parameter
   $ID=$_GET['ID'];
   //select data dari tabel data_mhs berdasarkan id
   $sql="SELECT * FROM data_mhs WHERE ID='$ID'";
   
   $query=sqlsrv_query($conn,$sql) or die(sqlsrv_errors());
   $data=sqlsrv_fetch_array($query);
  ?>
  <?php echo $data['NIM']; ?>
 </div

</body>
</html>
