<?php
include 'koneksi.php';

//ambil data id dari parameter
$ID=$_GET['id'];
//select data dari tabel data_mhs berdasarkan id
$sql="SELECT * FROM data_mhs WHERE ID_datamhs='$ID'";
   
$query=sqlsrv_query($conn,$sql) or die(sqlsrv_errors());
$data=sqlsrv_fetch_array($query);  
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
   <table>
    <form method="POST">
    </form>
   </table>
 </div>

  <?php
   //eksekusi simpan data
   if (isset($_POST['edit'])) {
    # code...
    //data ID data_mhs berasal dari select berdasarkan id
    $ID_datamhs=$data['ID_datamhs'];

    //data dari form
    $NIM=$_POST['NIM'];
    $Nama=$_POST['Nama'];
    $Prodi=$_POST['Prodi'];
    $TBT=$_POST['TBT'];

    $sql="UPDATE data_mhs SET NIM='$NIM', Nama='$Nama', Prodi='$Prodi', TBT='$TBT' WHERE ID_datamhs='$ID_datamhs'";
    $query=sqlsrv_query($conn,$sql) or die(sqlsrv_errors());
    if ($query) {
     //redirect ke halaman index
     echo "<script>alert('Data berhasil diedit!');</script>";
     echo "<meta http-equiv='refresh' content='0;url=index.php?datadiedit=sukses'>";
    } else {
     echo "<script>alert('Data gagal diedit!');</script>";
     echo "<meta http-equiv='refresh' content='0;url=index.php?datadiedit=gagal'>";
    }
   }
  ?>
 
</body>
</html>
