<?php
include 'koneksi.php';
?>

<!DOCTYPE html>
<html lang="en">
<head>
<title>UAS Aplikasi Mobile</title>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
  
  <div align="center">
  <h3>Simpan Data SQL Server dengan PHP<br>jancukers.com</h3>
  <a href="tambah.php">Tambah Data</a>
  <table border="1" width="700px">
   <thead>
    <tr>
     <th>No</th>
     <th>NIM</th>
     <th>Nama</th>
     <th>Program Studi</th>
     <th>Tanggal Bulan Tahun</th>
    </tr>
   </thead>
   <tbody>
  
  <?php
     // panggil file koneksi
     $sql="SELECT * FROM data_mhs";
     $no=1;
     //eksekusi query menampilkan data dari tabel Mhsw
     $query=sqlsrv_query($conn,$sql) or die(sqlsrv_errors());;
     //mengembalikan data row menjadi array dan looping data menggunakan while
     while ($data=sqlsrv_fetch_array($query)) {
    ?>
     <tr>
      <td><?php echo $no++; ?></td>
      <td><?php echo $data['NIM']; ?></td>
      <td><?php echo $data['Nama']; ?></td>
      <td><?php echo $data['Jurusan']; ?></td>
      <td><?php echo $data['TanggalBulanTahun']; ?></td>
      <td>
       <a href="edit.php?id=<?php echo $data['IDMhsw']; ?>">Edit</a> |
       <a href="hapus.php?id=<?php echo $data['IDMhsw']; ?>"  onClick="javascript: return confirm('Apakah anda yakin?');">Hapus</a> 
      </td>
     </tr>
    <?php } ?>
   </tbody>
  </table>
  </div>

<h1>This is a Heading</h1>
<p>This is a paragraph.</p>

</body>
</html>
